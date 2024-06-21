package ru.yandex.practicum.java.devext.kanban.task.management;

import ru.yandex.practicum.java.devext.kanban.history.HistoryManager;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static ru.yandex.practicum.java.devext.kanban.Managers.getDefaultHistory;


public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, SubTask> subTasks;
    protected final HistoryManager historyManager;
    protected AtomicInteger idSeq;
    protected final TreeSet<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        tasks = new ConcurrentHashMap<>();
        epics = new ConcurrentHashMap<>();
        subTasks = new ConcurrentHashMap<>();
        historyManager = getDefaultHistory();
        idSeq = new AtomicInteger();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartDateTime));
    }

    @Override
    public void addTask(Task newTask) {
        if (newTask.getStatus() == Status.NEW) {
            Optional<Task> overlap = tasks.values().stream()
                    .filter(task -> executionDateTimeOverlaps(task, newTask))
                    .findFirst();
            if (overlap.isPresent())
                throw new ExecutionDateTimeOverlapException("Задача " + newTask + " пересекается по времени выполнения с задачей "
                        + overlap.get());
            Optional<SubTask> subTaskOverlap = subTasks.values().stream()
                    .filter(subTask -> executionDateTimeOverlaps(subTask, newTask))
                    .findFirst();
            if (subTaskOverlap.isPresent())
                throw new ExecutionDateTimeOverlapException("Задача " + newTask + " пересекается по времени выполнения с подзадачей "
                        + subTaskOverlap.get());
            tasks.put(newTask.getId(), newTask);
            if (newTask.getStartDateTime() != null)
                prioritizedTasks.add(newTask);
        }
    }

    @Override
    public void addEpic(Epic newEpic) {
        if (newEpic.getStatus() == Status.NEW)
            epics.put(newEpic.getId(), newEpic);
        else
            throw new RuntimeException("Добавить можно только новый эпик");
    }

    @Override
    public void addSubTask(SubTask newSubTask, Epic epic) {
        if (newSubTask.getStatus() == Status.NEW) {
            Optional<Task> taskOverlap = tasks.values().stream()
                    .filter(task -> executionDateTimeOverlaps(task, newSubTask))
                    .findFirst();
            if (taskOverlap.isPresent())
                throw new ExecutionDateTimeOverlapException("Подзадача " + newSubTask + " пересекается по времени выполнения с задачей "
                        + taskOverlap.get());
            Optional<SubTask> subTaskOverlap = subTasks.values().stream()
                    .filter(subTask -> executionDateTimeOverlaps(subTask, newSubTask))
                    .findFirst();
            if (subTaskOverlap.isPresent())
                throw new ExecutionDateTimeOverlapException("Подзадача " + newSubTask + " пересекается по времени выполнения с подзадачей "
                        + subTaskOverlap.get());
            if (!epics.containsKey(epic.getId()))
                addEpic(epic);
            newSubTask.setEpicId(epic.getId());
            epic.bindSubTask(newSubTask);
            subTasks.put(newSubTask.getId(), newSubTask);
            if (newSubTask.getStartDateTime() != null) {
                prioritizedTasks.add(newSubTask);
                setEpicTimeline(epic);
            }
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
            SubTask subTask = subTasks.get(id);
            Epic epic = epics.get(subTask.getEpicId());
            epic.unbindSubTask(subTask);
            subTasks.remove(subTask.getId());
            setEpicTimeline(epic);
            historyManager.remove(subTask);
            int doneCounter = 0;
            Set<Integer> subtaskIds = epic.getSubTaskIds();
            for (int stId : subtaskIds)
                if (subTasks.get(stId).getStatus() == Status.DONE)
                    doneCounter++;
            if (subtaskIds.size() == doneCounter)
                epic.setStatus(Status.DONE);
        } else
            System.out.println("Ошибка: подзадача с ID " + id + " не существует");
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        int updatedId = epic.getId();
        if (epics.containsKey(updatedId)) {
            Set<Integer> subTaskIds = epic.getSubTaskIds();
            int newCounter = 0, doneCounter = 0;
            for (int stId : subTaskIds) {
                SubTask st = subTasks.get(stId);
                switch (st.getStatus()) {
                    case NEW -> newCounter++;
                    case DONE -> doneCounter++;
                }
                if (!subTaskIds.isEmpty() && subTaskIds.size() == newCounter)
                    epic.setStatus(Status.NEW);
                else if (!subTaskIds.isEmpty() && subTaskIds.size() == doneCounter)
                    epic.setStatus(Status.DONE);
                else
                    epic.setStatus(Status.IN_PROGRESS);
            }
            epics.put(updatedId, epic);
            setEpicTimeline(epic);
        } else
            System.out.println("Ошибка: эпик ещё не создан");
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        int updatedId = subTask.getId();
        if (subTasks.containsKey(updatedId)) {
            Epic epic = epics.get(subTask.getEpicId());
            subTasks.put(updatedId, subTask);
            updateEpic(epic);
        } else
            System.out.println("Ошибка: подзадача ещё не создана");
    }

    @Override
    public List<SubTask> getSubTasksForEpic(Epic epic) {
        return epic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public LinkedList<Task> getPrioritizedTasks() {
        return new LinkedList<>(prioritizedTasks);
    }

    @Override
    public int getNextId() {
        return idSeq.getAndIncrement();
    }

    protected final boolean executionDateTimeOverlaps(Task task1, Task task2) {
        if (task1.getStartDateTime() != null
                && task2.getStartDateTime() != null
                && task1.getDuration() != null
                && task2.getDuration() != null) {
            return task1.getEndDateTime().isAfter(task2.getStartDateTime()) && task2.getEndDateTime().isAfter(task1.getStartDateTime());
        } else
            return false;
    }

    private void setEpicTimeline(Epic epic) {
        // Продолжительность эпика — сумма продолжительностей всех его подзадач
        epic.setDuration(
                epic.getSubTaskIds().stream()
                        .map(subTasks::get)
                        .map(SubTask::getDuration)
                        .reduce(Duration.ZERO, Duration::plus)
        );
        // Время начала — дата старта самой ранней подзадачи
        epic.setStartDateTime(
                epic.getSubTaskIds().stream()
                        .map(subTasks::get)
                        .map(SubTask::getStartDateTime)
                        .filter(Objects::nonNull)
                        .min(LocalDateTime::compareTo)
                        .orElse(null)
        );
        // время завершения — время окончания самой поздней из задач
        epic.setEndDateTime(
                epic.getSubTaskIds().stream()
                        .map(subTasks::get)
                        .filter(st -> st.getStartDateTime() != null)
                        .map(SubTask::getEndDateTime)
                        .max(LocalDateTime::compareTo)
                        .orElse(null)
        );
    }
}