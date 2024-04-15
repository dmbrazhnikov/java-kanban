package ru.yandex.practicum.java.devext.kanban.task;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;


@Getter
public class Task {

    protected final int id;
    @Setter
    protected String name, description;
    @Setter
    protected Status status;
    protected static final AtomicInteger idSeq = new AtomicInteger();

    public Task(String name) {
        this.id = idSeq.getAndIncrement();
        this.name = name;
        status = Status.NEW;
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
        return id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
