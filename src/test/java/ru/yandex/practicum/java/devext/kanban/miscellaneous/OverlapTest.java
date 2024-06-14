package ru.yandex.practicum.java.devext.kanban.miscellaneous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.InMemoryTaskManager;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Метод определения пересечения сроков задач")
public class OverlapTest {

    private Task t1, t2;
    private InMemoryTaskManager tm;

    @BeforeEach
    void beforeEach() {
        tm = new InMemoryTaskManager();
        t1 = new Task(tm.getNextId(), "Test task 1");
        t2 = new Task(tm.getNextId(), "Test task 2");
    }

    @Test
    @DisplayName("Пересекающиеся")
    void overlap() {
        t1.setStartDateTime(LocalDateTime.now());
        t1.setDuration(Duration.ofHours(1));
        t2.setStartDateTime(LocalDateTime.now().plusMinutes(10));
        t2.setDuration(Duration.ofHours(1));
        boolean result1 = tm.executionDateTimeOverlaps(t1, t2), result2 = tm.executionDateTimeOverlaps(t2, t1);
        assertAll(
                () -> assertTrue(result1),
                () -> assertTrue(result2)
        );
    }

    @Test
    @DisplayName("Непересекающиеся")
    void noOverlap() {
        t1.setStartDateTime(LocalDateTime.now());
        t1.setDuration(Duration.ofHours(1));
        t2.setStartDateTime(t1.getEndDateTime().plusMinutes(10));
        t2.setDuration(Duration.ofHours(1));
        boolean result1 = tm.executionDateTimeOverlaps(t1, t2), result2 = tm.executionDateTimeOverlaps(t2, t1);
        assertAll(
                () -> assertFalse(result1),
                () -> assertFalse(result2)
        );
    }
}
