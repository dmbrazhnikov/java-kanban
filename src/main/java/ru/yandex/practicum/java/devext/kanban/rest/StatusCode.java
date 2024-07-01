package ru.yandex.practicum.java.devext.kanban.rest;

public enum StatusCode {

    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    INTERNAL_SERVER_ERROR(500);

    public final int value;

    StatusCode(int code) {
        value = code;
    }
}
