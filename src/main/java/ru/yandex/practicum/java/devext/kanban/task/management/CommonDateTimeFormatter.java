package ru.yandex.practicum.java.devext.kanban.task.management;

import lombok.Getter;
import java.time.format.DateTimeFormatter;

/** Форматтеры для получения типовых литералов даты-времени */
@Getter
public enum CommonDateTimeFormatter {

    ISO_LOCAL("yyyy-MM-dd'T'HH:mm:ss"),
    ISO_UTC("yyyy-MM-dd'T'HH:mm:ss'Z'"),
    ISO_OFFSET("yyyy-MM-dd'T'HH:mm:ssXXX"),
    ISO_OFFSET_MILLIS("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
    DATE("yyyy-MM-dd");

    private final DateTimeFormatter dtf;

    CommonDateTimeFormatter(String format) {
        dtf = DateTimeFormatter.ofPattern(format);
    }
}