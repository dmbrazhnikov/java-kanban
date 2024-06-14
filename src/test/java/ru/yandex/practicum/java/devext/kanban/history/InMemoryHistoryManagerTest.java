package ru.yandex.practicum.java.devext.kanban.history;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.java.devext.kanban.Managers;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Менеджер истории in-memory")
class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private List<Task> refTasks;
    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        taskManager = Managers.getDefault();
    }

    @Test
    @DisplayName("Класс менеджера истории по умолчанию")
    void defaultClass() {
        assertInstanceOf(InMemoryHistoryManager.class, historyManager);
    }

    @Test
    @DisplayName("Первичное добавление")
    @Description("Первичное добавление задачи в пустую историю просмотра")
    void addFirst() {
        Task t = new Task(taskManager.getNextId(), "Initial test task ");
        historyManager.add(t);
        assertTrue(historyManager.getHistory().contains(t));
    }

    @ParameterizedTest
    @DisplayName("Повторное добавление")
    @Description("Повторное добавление просмотренной ранее задачи в непустую историю просмотра")
    @ValueSource(ints = {21})
    void addAlreadyViewed(int tasksQuantity) {
        // Подготовка
        refTasks = getTestTasks(tasksQuantity);
        refTasks.forEach(historyManager::add);
        Task randomTask = refTasks.get(ThreadLocalRandom.current().nextInt(0, refTasks.size()));
        // Выполнение
        long startTime = System.nanoTime();
        historyManager.add(randomTask);
        long stopTime = System.nanoTime();
        System.out.println("Execution time for " + tasksQuantity + " task(s): " + (stopTime - startTime) + " nanoseconds");
        // Проверка
        assertTrue(historyManager.getHistory().contains(randomTask));
    }

    @Test
    @DisplayName("Корректность длины и порядка")
    @Description("Проверка равенства количества элементов и их порядка в истории просмотра")
    void getHistory() {
        refTasks = getTestTasks(21);
        refTasks.forEach(t -> historyManager.add(t));
        List<Task> actualHistory = historyManager.getHistory();
        assertThat(actualHistory, containsInRelativeOrder(refTasks.toArray()));
    }

    private List<Task> getTestTasks(int tasksQuantity) {
        List<Task> tasks = new LinkedList<>();
        for (int i = 1; i <= tasksQuantity; i++) {
            Task t;
            if (i % 2 == 0)
                t = new SubTask(taskManager.getNextId(), "Test subtask " + i);
            else if (i % 3 == 0)
                t = new Epic(taskManager.getNextId(), "Test epic " + i);
            else
                t = new Task(taskManager.getNextId(), "Test task " + i);
            tasks.add(t);
        }
        return tasks;
    }
}