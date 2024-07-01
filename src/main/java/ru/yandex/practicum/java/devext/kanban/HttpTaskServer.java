package ru.yandex.practicum.java.devext.kanban;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.java.devext.kanban.rest.*;
import ru.yandex.practicum.java.devext.kanban.task.management.Managers;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class HttpTaskServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 3);
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
        httpServer.createContext("/stop", new StopServerHandler(httpServer));
        httpServer.start();
        log.info("Server started at " + PORT);
    }
}
