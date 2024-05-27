package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path backupFilePath;

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;
    }

    @Override
    public void addTask(Task t) {
        super.addTask(t);
        save();
    }

    @Override
    public void addEpic(Epic e) {
        super.addEpic(e);
        save();
    }

    @Override
    public void addSubTask(SubTask st, Epic e) {
        super.addSubTask(st, e);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubTask(int id) {
        super.removeSubTask(id);
        save();
    }

    @Override
    public void updateTask(Task updated) {
        super.updateTask(updated);
        save();
    }

    @Override
    public void updateEpic(Epic updated) {
        super.updateEpic(updated);
        save();
    }

    @Override
    public void updateSubTask(SubTask updated) {
        super.updateSubTask(updated);
        save();
    }

    private void save() {
        // TODO Реализация
    }
}
