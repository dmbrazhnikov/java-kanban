package ru.yandex.practicum.java.devext.kanban;

import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class BaseTest {

    protected void addEpicWithSubTasks(List<Task> refEpics, List<Task> refSubTasks, TaskManager tm, int epicNum, int subTasksPerEpic) {
        LocalDateTime endDT = findMaxEndDateTime(tm);
        for (int i = 0; i < epicNum; i++) {
            Epic e = new Epic(tm.getNextId(), "Test epic " + (i + 1));
            refEpics.add(e);
            tm.addEpic(e);
            for (int j = 0; j < subTasksPerEpic; j++) {
                SubTask st = new SubTask(tm.getNextId(), "Subtask " + i + j);
                st.setStartDateTime(endDT.plusMinutes(10));
                st.setDuration(Duration.ofHours(1));
                refSubTasks.add(st);
                tm.addSubTask(st, e);
                endDT = st.getEndDateTime();
            }
        }
    }

    protected void addTasks(List<Task> refTasks, TaskManager tm, int tasksNum) {
        LocalDateTime endDT = findMaxEndDateTime(tm);
        for (int i = 0; i < tasksNum; i++) {
            Task t = new Task(tm.getNextId(), "Test task " + (i + 1));
            t.setStartDateTime(endDT.plusMinutes(10));
            t.setDuration(Duration.ofHours(1));
            refTasks.add(t);
            tm.addTask(t);
            endDT = t.getEndDateTime();
        }
    }

    protected void addSubtasksForEpic(List<Task> refTasks, TaskManager tm, Epic e, int subTaskNum) {
        LocalDateTime endDT = LocalDateTime.now();
        for (int i = 0; i < subTaskNum; i++) {
            SubTask st = new SubTask(tm.getNextId(), "Test subtask " + (i + 1));
            st.setStartDateTime(endDT.plusMinutes(10));
            st.setDuration(Duration.ofHours(1));
            refTasks.add(st);
            tm.addSubTask(st, e);
            endDT = st.getEndDateTime();
        }
    }

    private LocalDateTime findMaxEndDateTime(TaskManager tm) {
        LocalDateTime maxTaskEndDT = tm.getTasks().stream()
                .map(Task::getEndDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        LocalDateTime maxSubTaskEndDT = tm.getSubTasks().stream()
                .map(SubTask::getEndDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        return maxTaskEndDT.isBefore(maxSubTaskEndDT) ? maxSubTaskEndDT : maxTaskEndDT;
    }
}
