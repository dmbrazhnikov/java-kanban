package ru.yandex.practicum.java.devext.kanban.task.management;

public class ExecutionDateTimeOverlapException extends RuntimeException {
    public ExecutionDateTimeOverlapException(String message) {
        super(message);
    }
}
