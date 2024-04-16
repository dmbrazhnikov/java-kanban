package ru.yandex.practicum.java.devext.kanban.history;

import lombok.Getter;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {

    private final LinkedList<Task> items;
    @Getter
    private final int capacity;

    public InMemoryHistoryManager(int capacity) {
        items = new LinkedList<>();
        this.capacity = capacity;
    }

    @Override
    public void add(Task element) {
        if (element != null) {
            if (items.size() == capacity)
                items.removeFirst();
            items.addLast(element);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(items);
    }
}
