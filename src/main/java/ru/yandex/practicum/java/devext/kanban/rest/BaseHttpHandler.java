package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;
    protected Gson gson;

    protected BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls();
        gson = gsonBuilder.create();
    }

    protected void sendText(HttpExchange ex, String text, StatusCode rCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        ex.sendResponseHeaders(rCode.value, resp.length);
        ex.getResponseBody().write(resp);
        ex.close();
    }

    protected void sendEmptyResponse(HttpExchange ex, StatusCode statusCode) throws IOException {
        ex.sendResponseHeaders(statusCode.value, 0);
        ex.close();
    }
}
