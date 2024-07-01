package ru.yandex.practicum.java.devext.kanban.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


@Setter
@Getter
@ToString
public class Task {

    protected int id = -1;
    protected Duration duration;
    protected LocalDateTime startDateTime;
    protected String name, description;
    protected TaskStatus status;

    public Task(int id, String name) {
        this.id = id;
        this.name = name;
        status = TaskStatus.NEW;
    }

    public Task(String name) {
        this.name = name;
        status = TaskStatus.NEW;
        startDateTime = LocalDateTime.now();
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plus(duration);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task t = (Task) o;
        return id == t.getId();
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
