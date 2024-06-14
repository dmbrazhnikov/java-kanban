package ru.yandex.practicum.java.devext.kanban.task.management.filebacked;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message) {
        super(message);
    }
}
