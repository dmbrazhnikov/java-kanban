package ru.yandex.practicum.java.devext.kanban;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@DisplayName("Интеграционные тесты менеджеров задач и истории")
public class InMemoryTaskManagerIntegrationTest {

    private TaskManager taskManager;
    private ArrayList<Task> refTasks;
    private LinkedList<Task> refHistory;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        refTasks = new ArrayList<>();
        refHistory = new LinkedList<>();
        // Подготавливаем 3 простые задачи, 3 эпика и по 3 подзадачи на эпик, итого 12 задач.
        for (int i = 0; i < 3; i++) {
            Task t = new Task("Test task " + (i + 1));
            refTasks.add(t);
            taskManager.addTask(t);
        }
        for (int i = 0; i < 3; i++) {
            Epic e = new Epic("Test epic " + (i + 1));
            refTasks.add(e);
            taskManager.addEpic(e);
            for (int j = 0; j < 3; j++) {
                SubTask st = new SubTask("Subtask " + i + j);
                refTasks.add(st);
                taskManager.addSubTask(st, e);
            }
        }
        for (int i = 0; i < refTasks.size(); i++) {
            Task randomTask = refTasks.get(ThreadLocalRandom.current().nextInt(0, refTasks.size()));
            if (refHistory.contains(randomTask))
                refHistory.remove(randomTask);
            refHistory.addLast(randomTask);
            int randomTaskId = randomTask.getId();
            if (randomTask instanceof Epic)
                taskManager.getEpicById(randomTaskId);
            else if (randomTask instanceof SubTask)
                taskManager.getSubTaskById(randomTaskId);
            else
                taskManager.getTaskById(randomTaskId);
        }
    }

    @Test
    @DisplayName("Длина и порядок истории")
    void historyPopulation() {
        List<Task> actualHistory = taskManager.getHistory();
        assertAll(
                () -> assertEquals(refHistory.size(), actualHistory.size()),
                () -> assertEquals(refHistory, actualHistory)
        );
    }
}