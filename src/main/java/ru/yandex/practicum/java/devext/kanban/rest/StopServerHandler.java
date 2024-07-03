package ru.yandex.practicum.java.devext.kanban.rest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StopServerHandler implements HttpHandler {

    private final HttpServer server;

    public StopServerHandler(HttpServer server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(200, 0);
        ex.close();
        server.stop(0);
        log.info("Server stopped");
    }
}
