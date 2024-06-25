package ru.yandex.practicum.java.devext.kanban;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.java.devext.kanban.rest.*;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 3);
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
        httpServer.start();
    }
}
