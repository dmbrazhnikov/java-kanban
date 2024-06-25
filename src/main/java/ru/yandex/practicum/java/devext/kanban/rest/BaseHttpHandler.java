package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;
    protected Gson gson;

    protected BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new Gson();
    }

    protected void sendText(HttpExchange ex, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        ex.sendResponseHeaders(200, resp.length);
        ex.getResponseBody().write(resp);
        ex.close();
    }

    protected void sendNotFound(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(404, 0);
        ex.close();
    }

    protected void sendHasOverlaps(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(406, 0);
        ex.close();
    }

    protected void sendBadRequest(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(400, 0);
        ex.close();
    }
}
