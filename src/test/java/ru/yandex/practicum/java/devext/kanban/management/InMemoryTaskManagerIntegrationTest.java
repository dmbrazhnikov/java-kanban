package ru.yandex.practicum.java.devext.kanban.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.devext.kanban.BaseTest;
import ru.yandex.practicum.java.devext.kanban.task.management.Managers;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@DisplayName("Интеграционные тесты менеджеров задач и истории")
public class InMemoryTaskManagerIntegrationTest extends BaseTest {

    private TaskManager taskManager;
    private LinkedList<Task> refHistory;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        ArrayList<Task> refTasks = new ArrayList<>();
        refHistory = new LinkedList<>();
        // Подготавливаем 3 простые задачи, 3 эпика и по 3 подзадачи на эпик, итого 12 задач.
        addTasks(refTasks, taskManager, 3);
        addEpicWithSubTasks(refTasks, refTasks, taskManager, 3, 3);
        for (int i = 0; i < refTasks.size(); i++) {
            Task randomTask = refTasks.get(ThreadLocalRandom.current().nextInt(0, refTasks.size()));
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

    @Test
    @DisplayName("Удаление из истории при удалении задачи")
    void removeFromHistory() {
        Task t = new Task(taskManager.getNextId(), "Test task");
        t.setStartDateTime(LocalDateTime.now().minusDays(1));
        t.setDuration(Duration.ofHours(1));
        taskManager.addTask(t);
        taskManager.getTaskById(t.getId());
        taskManager.removeTask(t.getId());
        List<Task> actualHistory = taskManager.getHistory();
        assertFalse(actualHistory.contains(t));
    }
}
