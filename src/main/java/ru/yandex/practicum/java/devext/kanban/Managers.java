package ru.yandex.practicum.java.devext.kanban;

import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.history.HistoryManager;
import ru.yandex.practicum.java.devext.kanban.history.InMemoryHistoryManager;
import ru.yandex.practicum.java.devext.kanban.task.management.InMemoryTaskManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
