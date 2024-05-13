package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.history.HistoryManager;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static ru.yandex.practicum.java.devext.kanban.Managers.getDefaultHistory;


public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new ConcurrentHashMap<>();
        epics = new ConcurrentHashMap<>();
        subTasks = new ConcurrentHashMap<>();
        historyManager = getDefaultHistory();
    }

    @Override
    public void addTask(Task t) {
        if (t.getStatus() == Status.NEW)
            tasks.put(t.getId(), t);
    }

    @Override
    public void addEpic(Epic e) {
        if (e.getStatus() == Status.NEW)
            epics.put(e.getId(), e);
        else
            throw new RuntimeException("Добавить можно только новый эпик");
    }

    @Override
    public void addSubTask(SubTask st, Epic e) {
        if (st.getStatus() == Status.NEW) {
            if (!epics.containsKey(e.getId()))
                addEpic(e);
            st.setEpicId(e.getId());
            e.bindSubTask(st);
            subTasks.put(st.getId(), st);
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void removeAllTasks() {
        tasks.forEach((id, task) -> {
            tasks.remove(id);
            historyManager.remove(task);
        });
    }

    @Override
    public void removeAllEpics() {
        epics.forEach((id, epic) -> {
            removeEpic(id);
            historyManager.remove(epic);
        });
    }

    @Override
    public void removeAllSubTasks() {
        subTasks.forEach((id, subtask) -> {
            removeSubTask(id);
            historyManager.remove(subtask);
        });
    }

    @Override
    public void removeTask(int id) {
        if (!tasks.isEmpty() && tasks.get(id) != null) {
            historyManager.remove(tasks.get(id));
            tasks.remove(id);
        } else
            System.out.println("Ошибка: задача с ID " + id + " не существует");
    }

    @Override
    public void removeEpic(int id) {
        if (!epics.isEmpty() && epics.get(id) != null) {
            Set<Integer> subtaskIds = epics.get(id).getSubTaskIds();
            int doneCounter = 0;
            for (int stId : subtaskIds)
                if (subTasks.get(stId).getStatus() == Status.DONE)
                    doneCounter++;
            if (subtaskIds.isEmpty() || subtaskIds.size() == doneCounter) {
                historyManager.remove(epics.get(id));
                epics.remove(id);
            } else
                System.out.println("Ошибка: эпик не не может быть удалён, поскольку не завершён или содержит подзадачи");
        } else
            System.out.println("Ошибка: эпик с ID " + id + " не существует");
    }

    @Override
    public void removeSubTask(int id) {
        if (!subTasks.isEmpty() && subTasks.get(id) != null) {
            SubTask st = subTasks.get(id);
            Epic e = epics.get(st.getEpicId());
            e.unbindSubTask(st);
            subTasks.remove(st.getId());
            historyManager.remove(st);
            int doneCounter = 0;
            Set<Integer> subtaskIds = e.getSubTaskIds();
            for (int stId : subtaskIds)
                if (subTasks.get(stId).getStatus() == Status.DONE)
                    doneCounter++;
            if (subtaskIds.size() == doneCounter)
                e.setStatus(Status.DONE);
        } else
            System.out.println("Ошибка: подзадача с ID " + id + " не существует");
    }

    @Override
    public Task getTaskById(Integer id) {
        Task t = tasks.get(id);
        historyManager.add(t);
        return t;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic e = epics.get(id);
        historyManager.add(e);
        return e;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        SubTask st = subTasks.get(id);
        historyManager.add(st);
        return st;
    }

    @Override
    public void updateTask(Task updated) {
        tasks.put(updated.getId(), updated);
    }

    @Override
    public void updateEpic(Epic updated) {
        int updatedId = updated.getId();
        if (epics.containsKey(updatedId)) {
            Set<Integer> subTaskIds = updated.getSubTaskIds();
            int newCounter = 0, doneCounter = 0;
            for (int stId : subTaskIds) {
                SubTask st = subTasks.get(stId);
                switch (st.getStatus()) {
                    case NEW -> newCounter++;
                    case DONE -> doneCounter++;
                }
                if (!subTaskIds.isEmpty() && subTaskIds.size() == newCounter)
                    updated.setStatus(Status.NEW);
                else if (!subTaskIds.isEmpty() && subTaskIds.size() == doneCounter)
                    updated.setStatus(Status.DONE);
                else
                    updated.setStatus(Status.IN_PROGRESS);
            }
            epics.put(updatedId, updated);
        } else
            System.out.println("Ошибка: эпик ещё не создан");
    }

    @Override
    public void updateSubTask(SubTask updated) {
        int updatedId = updated.getId();
        if (subTasks.containsKey(updatedId)) {
            subTasks.put(updatedId, updated);
            updateEpic(epics.get(updated.getEpicId()));
        } else
            System.out.println("Ошибка: подзадача ещё не создана");
    }

    @Override
    public List<SubTask> getSubTasksForEpic(Epic e) {
        List<SubTask> result = new ArrayList<>();
        for (int stId : e.getSubTaskIds()) {
            SubTask st = subTasks.get(stId);
            if (st != null)
                result.add(st);
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}