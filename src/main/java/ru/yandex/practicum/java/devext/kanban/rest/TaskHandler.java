package ru.yandex.practicum.java.devext.kanban.rest;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.NotFoundException;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;

import java.io.IOException;
import java.util.List;

@Slf4j
public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        RequestMethod method = RequestMethod.valueOf(ex.getRequestMethod());
        String[] pathSegments = ex.getRequestURI().getPath().substring(1).split("/");
        switch (pathSegments.length) {
            case 1 -> { // /tasks
                switch (method) {
                    case GET -> {
                        List<Task> tasks = taskManager.getTasks();
                        sendText(ex, gson.toJson(tasks));
                    }
                    case POST -> {
                        // TODO createTask() если айди не указан, updateTask() если указан
                    }
                }
            }
            case 2 -> { // /tasks/{id}
                try {
                    int taskId = Integer.parseInt(pathSegments[1]);
                    Task task = taskManager.getTaskById(taskId);
                    sendText(ex, gson.toJson(task));
                } catch (NumberFormatException e) {
                    sendBadRequest(ex);
                } catch (NotFoundException e) {
                    sendNotFound(ex);
                }
            }
        }

    }
}
