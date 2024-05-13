package ru.yandex.practicum.java.devext.kanban.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


@Getter
@ToString
public class Task {

    protected final int id;
    protected static final AtomicInteger idSeq = new AtomicInteger();
    @Setter
    protected String name, description;
    @Setter
    protected Status status;

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
        return Objects.hashCode(id);
    }
}
