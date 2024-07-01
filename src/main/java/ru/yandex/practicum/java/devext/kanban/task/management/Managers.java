package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.unit.history.HistoryManager;
import ru.yandex.practicum.java.devext.kanban.unit.history.InMemoryHistoryManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
