package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.ExecutionDateTimeOverlapException;
import ru.yandex.practicum.java.devext.kanban.task.management.NotFoundException;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.task.management.filebacked.ManagerSaveException;
import java.io.IOException;
import java.util.List;
import static java.nio.charset.StandardCharsets.*;
import static ru.yandex.practicum.java.devext.kanban.rest.StatusCode.*;


@Slf4j
public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        RequestMethod method = RequestMethod.valueOf(ex.getRequestMethod());
        String[] pathSegments = ex.getRequestURI().getPath().substring(1).split("/");
        Task task;
        try {
            switch (pathSegments.length) {
                case 1 -> { // /tasks
                    switch (method) {
                        case GET -> {
                            List<Task> tasks = taskManager.getTasks();
                            sendText(ex, gson.toJson(tasks), OK);
                        }
                        case POST -> {
                            String rqBody = new String(ex.getRequestBody().readAllBytes(), UTF_8);
                            task = gson.fromJson(rqBody, Task.class);
                            if (task.getId() == -1)
                                task.setId(taskManager.getNextId());
                            taskManager.addTask(task);
                            sendText(ex, gson.toJson(task), CREATED);
                            log.info("Task successfully created:\n{}", task);
                        }
                        default -> sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
                    }
                }
                case 2 -> { // /tasks/{id}
                    int taskId = Integer.parseInt(pathSegments[1]);
                    switch (method) {
                        case GET -> {
                            task = taskManager.getTaskById(taskId);
                            sendText(ex, gson.toJson(task), OK);
                        }
                        case PUT -> {
                            task = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    Task.class
                            );
                            taskManager.updateTask(task);
                            sendEmptyResponse(ex, CREATED);
                            log.info("Task successfully updated:\n{}", task);
                        }
                        case DELETE -> {
                            task = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    Task.class
                            );
                            taskManager.removeTask(task.getId());
                            sendEmptyResponse(ex, OK);
                            log.info("Task successfully removed:\n{}", task);
                        }
                        default -> sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
                    }
                }
                default -> sendEmptyResponse(ex, BAD_REQUEST);
            }
        } catch (JsonSyntaxException | NumberFormatException e) {
            sendEmptyResponse(ex, BAD_REQUEST);
        } catch (ExecutionDateTimeOverlapException e) {
            sendEmptyResponse(ex, NOT_ACCEPTABLE);
        } catch (NotFoundException e) {
            sendEmptyResponse(ex, NOT_FOUND);
        } catch (ManagerSaveException e) {
            sendEmptyResponse(ex, INTERNAL_SERVER_ERROR);
        }
    }
}
