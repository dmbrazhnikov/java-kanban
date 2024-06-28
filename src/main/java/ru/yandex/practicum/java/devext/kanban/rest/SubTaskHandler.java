package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.management.ExecutionDateTimeOverlapException;
import ru.yandex.practicum.java.devext.kanban.task.management.NotFoundException;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.task.management.filebacked.ManagerSaveException;
import java.io.IOException;
import java.util.List;
import static java.nio.charset.StandardCharsets.*;
import static ru.yandex.practicum.java.devext.kanban.rest.StatusCode.*;


@Slf4j
public class SubTaskHandler extends BaseHttpHandler {

    public SubTaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        RequestMethod method = RequestMethod.valueOf(ex.getRequestMethod());
        String[] pathSegments = ex.getRequestURI().getPath().substring(1).split("/");
        SubTask subTask;
        try {
            switch (pathSegments.length) {
                case 1 -> { // /subtasks
                    switch (method) {
                        case GET -> {
                            List<SubTask> subTasks = taskManager.getSubTasks();
                            sendText(ex, gson.toJson(subTasks));
                        }
                        case POST -> {
                            subTask = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    SubTask.class
                            );
                            taskManager.addSubTask(subTask, taskManager.getEpicById(subTask.getEpicId()));
                            sendEmptyResponse(ex, CREATED);
                            log.info("Subtask successfully created:\n{}", subTask);
                        }
                        default -> sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
                    }
                }
                case 2 -> { // /subtasks/{id}
                    int subTaskId = Integer.parseInt(pathSegments[1]);
                    switch (method) {
                        case GET -> {
                            subTask = taskManager.getSubTaskById(subTaskId);
                            sendText(ex, gson.toJson(subTask));
                        }
                        case PUT -> {
                            subTask = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    SubTask.class
                            );
                            taskManager.updateSubTask(subTask);
                            sendEmptyResponse(ex, CREATED);
                            log.info("Subtask successfully updated:\n{}", subTask);
                        }
                        case DELETE -> {
                            subTask = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    SubTask.class
                            );
                            taskManager.removeSubTask(subTask.getId());
                            sendEmptyResponse(ex, OK);
                            log.info("Subtask successfully removed:\n{}", subTask);
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
