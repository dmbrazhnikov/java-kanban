package ru.yandex.practicum.java.devext.kanban.unit.history;

import ru.yandex.practicum.java.devext.kanban.task.Task;
import java.util.List;


public interface HistoryManager {

    void add(Task element);

    List<Task> getHistory();

    void remove(Task element);
}