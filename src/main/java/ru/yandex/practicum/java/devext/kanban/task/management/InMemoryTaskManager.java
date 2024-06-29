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
import static ru.yandex.practicum.java.devext.kanban.task.management.Managers.getDefaultHistory;


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
    public void addTask(Task newTask) throws ExecutionDateTimeOverlapException {
        if (newTask.getStatus() == Status.NEW) {
            checkTaskExecDateTimeOverlaps(newTask);
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
    public void addSubTask(SubTask newSubTask, Epic epic) throws ExecutionDateTimeOverlapException {
        if (newSubTask.getStatus() == Status.NEW) {
            checkSubTaskExecDateTimeOverlaps(newSubTask);
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
        Optional<Task> opt = Optional.ofNullable(tasks.get(id));
        Task task = opt.orElseThrow(() -> new NotFoundException("Task with ID " + id + " is not created yet"));
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Optional<Epic> opt = Optional.ofNullable(epics.get(id));
        Epic epic = opt.orElseThrow(() -> new NotFoundException("Epic with ID " + id + " is not created yet"));
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        Optional<SubTask> opt = Optional.ofNullable(subTasks.get(id));
        SubTask subTask = opt.orElseThrow(() -> new NotFoundException("SubTask with ID " + id + " is not created yet"));
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public void updateTask(Task updatedTask) throws ExecutionDateTimeOverlapException {
        checkTaskExecDateTimeOverlaps(updatedTask);
        int updatedId = updatedTask.getId();
        if (tasks.containsKey(updatedId))
            tasks.put(updatedTask.getId(), updatedTask);
        else
            throw new NotFoundException("Task with ID " + updatedId + " is not created yet");
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
            throw new NotFoundException("Epic with ID " + updatedId + " is not created yet");
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) throws ExecutionDateTimeOverlapException {
        checkSubTaskExecDateTimeOverlaps(updatedSubTask);
        int updatedId = updatedSubTask.getId();
        if (subTasks.containsKey(updatedId)) {
            Epic epic = epics.get(updatedSubTask.getEpicId());
            subTasks.put(updatedId, updatedSubTask);
            updateEpic(epic);
        } else
            throw new NotFoundException("SubTask with ID " + updatedId + " is not created yet");
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

    protected void checkTaskExecDateTimeOverlaps(Task taskToCheck) {
        Optional<Task> overlap = tasks.values().stream()
                .filter(task -> !task.equals(taskToCheck))
                .filter(task -> executionDateTimeOverlaps(task, taskToCheck))
                .findFirst();
        if (overlap.isPresent())
            throw new ExecutionDateTimeOverlapException("Задача " + taskToCheck + " пересекается по времени выполнения с задачей "
                    + overlap.get());
        Optional<SubTask> subTaskOverlap = subTasks.values().stream()
                .filter(subTask -> executionDateTimeOverlaps(subTask, taskToCheck))
                .findFirst();
        if (subTaskOverlap.isPresent())
            throw new ExecutionDateTimeOverlapException("Задача " + taskToCheck + " пересекается по времени выполнения с подзадачей "
                    + subTaskOverlap.get());
    }

    protected void checkSubTaskExecDateTimeOverlaps(SubTask subTaskToCheck) {
        Optional<Task> taskOverlap = tasks.values().stream()
                .filter(task -> executionDateTimeOverlaps(task, subTaskToCheck))
                .findFirst();
        if (taskOverlap.isPresent())
            throw new ExecutionDateTimeOverlapException("Подзадача " + subTaskToCheck + " пересекается по времени выполнения с задачей "
                    + taskOverlap.get());
        Optional<SubTask> subTaskOverlap = subTasks.values().stream()
                .filter(subTask -> !subTask.equals(subTaskToCheck))
                .filter(subTask -> executionDateTimeOverlaps(subTask, subTaskToCheck))
                .findFirst();
        if (subTaskOverlap.isPresent())
            throw new ExecutionDateTimeOverlapException("Подзадача " + subTaskToCheck + " пересекается по времени выполнения с подзадачей "
                    + subTaskOverlap.get());
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