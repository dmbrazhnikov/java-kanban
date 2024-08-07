package ru.yandex.practicum.java.devext.kanban.rest;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import java.io.IOException;
import static ru.yandex.practicum.java.devext.kanban.rest.StatusCode.*;


public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        RequestMethod method = RequestMethod.valueOf(ex.getRequestMethod());
        if (method == RequestMethod.GET) {
            sendText(ex, gson.toJson(taskManager.getPrioritizedTasks()), OK);
        } else
            sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
    }
}
