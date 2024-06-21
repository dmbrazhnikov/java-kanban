package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.util.LinkedList;
import java.util.List;

public interface TaskManager {
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subTask, Epic epic);

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

    void updateTask(Task updatedTask);

    void updateEpic(Epic updatedEpic);

    void updateSubTask(SubTask updatedSubTask);

    List<SubTask> getSubTasksForEpic(Epic epic);

    List<Task> getHistory();

    int getNextId();

    LinkedList<Task> getPrioritizedTasks();
}
