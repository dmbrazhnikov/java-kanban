package ru.yandex.practicum.java.devext.kanban.task.management;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
