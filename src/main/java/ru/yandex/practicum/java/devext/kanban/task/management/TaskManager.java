package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    void addTask(Task t);

    void addEpic(Epic e);

    void addSubTask(SubTask st, Epic e);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasks();

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubTasks();

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubTask(int id);

    Task getTaskById(Integer id);

    Epic getEpicById(Integer id);

    SubTask getSubTaskById(Integer id);

    void updateTask(Task updated);

    void updateEpic(Epic updated);

    void updateSubTask(SubTask updated);

    List<SubTask> getSubTasksForEpic(Epic e);

    List<Task> getHistory();

    int getNextId();

    LinkedList<Task> getPrioritizedTasks();
}
