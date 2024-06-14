package ru.yandex.practicum.java.devext.kanban.task.management;

public class TimelineOverlapException extends RuntimeException {
    public TimelineOverlapException(String message) {
        super(message);
    }
}
