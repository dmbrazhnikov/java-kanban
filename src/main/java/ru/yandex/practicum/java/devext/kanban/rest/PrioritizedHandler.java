package ru.yandex.practicum.java.devext.kanban.rest;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
