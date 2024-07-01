package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.management.ExecutionDateTimeOverlapException;
import ru.yandex.practicum.java.devext.kanban.task.management.NotFoundException;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.task.management.filebacked.ManagerSaveException;
import java.io.IOException;
import java.util.List;
import static java.nio.charset.StandardCharsets.*;
import static ru.yandex.practicum.java.devext.kanban.rest.RequestMethod.*;
import static ru.yandex.practicum.java.devext.kanban.rest.StatusCode.*;


@Slf4j
public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        RequestMethod method = RequestMethod.valueOf(ex.getRequestMethod());
        String[] pathSegments = ex.getRequestURI().getPath().substring(1).split("/");
        Epic epic;
        try {
            switch (pathSegments.length) {
                case 1 -> { // /epics
                    switch (method) {
                        case GET -> {
                            List<Epic> epics = taskManager.getEpics();
                            sendText(ex, gson.toJson(epics), OK);
                        }
                        case POST -> {
                            epic = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    Epic.class
                            );
                            if (epic.getId() == -1)
                                epic.setId(taskManager.getNextId());
                            taskManager.addEpic(epic);
                            sendText(ex, gson.toJson(epic), CREATED);
                            log.info("Epic successfully created:\n{}", epic);
                        }
                        default -> sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
                    }
                }
                case 2 -> { // /epics/{id}
                    int epicId = Integer.parseInt(pathSegments[1]);
                    switch (method) {
                        case GET -> {
                            epic = taskManager.getEpicById(epicId);
                            sendText(ex, gson.toJson(epic), OK);
                        }
                        case DELETE -> {
                            epic = gson.fromJson(
                                    new String(ex.getRequestBody().readAllBytes(), UTF_8),
                                    Epic.class
                            );
                            taskManager.removeEpic(epic.getId());
                            sendEmptyResponse(ex, OK);
                            log.info("Epic successfully updated:\n{}", epic);
                        }
                        default -> sendEmptyResponse(ex, METHOD_NOT_ALLOWED);
                    }
                }
                case 3 -> { // /epics/{id}/subtasks
                    int epicId = Integer.parseInt(pathSegments[1]);
                    if (method == GET && pathSegments[2].equals("subtasks")) {
                        epic = taskManager.getEpicById(epicId);
                        List<SubTask> subTasks = taskManager.getSubTasks().stream()
                                .filter(st -> epic.getSubTaskIds().contains(st.getId()))
                                .toList();
                        sendText(ex, gson.toJson(subTasks), OK);
                    } else
                        sendEmptyResponse(ex, BAD_REQUEST);
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
