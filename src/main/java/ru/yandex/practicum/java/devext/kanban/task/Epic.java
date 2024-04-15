package ru.yandex.practicum.java.devext.kanban.task;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


@Getter
public class Epic extends Task {

    private final Set<Integer> subTaskIds;

    public Epic(String name) {
        super(name);
        subTaskIds = new HashSet<>();
    }

    public void bindSubTask(SubTask subTask) {
        subTaskIds.add(subTask.getId());
    }

    public void unbindSubTask(SubTask subTask) {
        subTaskIds.remove(subTask.getId());
    }

    @Override
    public String toString() {
        return "Epic{id=" + id +
                ", subTaskIds=" + subTaskIds +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
