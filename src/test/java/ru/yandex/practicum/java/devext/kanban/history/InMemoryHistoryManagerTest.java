package ru.yandex.practicum.java.devext.kanban.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.devext.kanban.Managers;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Менеджер истории in-memory")
class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private List<Task> tasks;
    private int tasksToViewLimit;

    @BeforeEach
    void beforeEach() {
        tasks = new LinkedList<>();
        historyManager = Managers.getDefaultHistory();
        tasksToViewLimit = historyManager.getCapacity() + 1;
        for (int i = 1; i <= tasksToViewLimit; i++) {
            Task t;
            if (i % 2 == 0)
                t = new SubTask("Test subtask " + i);
            else if (i % 3 == 0)
                t = new Epic("Test epic " + i);
            else
                t = new Task("Test task " + i);
            tasks.add(t);
        }
    }

    @Test
    @DisplayName("Класс менеджера истории по умолчанию")
    void defaultClass() {
        assertInstanceOf(InMemoryHistoryManager.class, historyManager);
    }

    @Test
    @DisplayName("Добавление в историю просмотра")
    void add() {
        Task randViewed = tasks.get(ThreadLocalRandom.current().nextInt(0, tasksToViewLimit));
        historyManager.add(randViewed);
        assertTrue(historyManager.getHistory().contains(randViewed));
    }

    @Test
    @DisplayName("История просмотра")
    void getHistory() {
        tasks.forEach(t -> historyManager.add(t));
        List<Task> actualHistory = historyManager.getHistory();
        tasks.removeFirst();
        assertThat(actualHistory, containsInRelativeOrder(tasks.toArray()));
    }
}