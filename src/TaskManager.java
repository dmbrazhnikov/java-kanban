import java.util.*;


public class TaskManager {

    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;

    TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public void addTask(Task t) {
        if (t.getStatus() == Status.NEW)
            tasks.put(t.getId(), t);
    }

    public void addEpic(Epic e) {
        if (e.getStatus() == Status.NEW)
            epics.put(e.getId(), e);
        else
            throw new RuntimeException("Добавить можно только новый эпик");
    }

    public void addSubTask(SubTask st, Epic e) {
        if (st.getStatus() == Status.NEW) {
            if (!epics.containsKey(e.getId()))
                addEpic(e);
            st.setEpicId(e.getId());
            e.bindSubTask(st);
            subTasks.put(st.getId(), st);
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpics() {
        epics.forEach((id, epic) -> removeEpic(id));
    }

    public void removeAllSubTasks() {
        subTasks.clear();
    }

    public void removeTask(int id) {
        if (!tasks.isEmpty() && tasks.get(id) != null)
            tasks.remove(id);
        else
            System.out.println("Ошибка: задача с ID " + id + " не существует");
    }

    public void removeEpic(int id) {
        if (!epics.isEmpty() && epics.get(id) != null) {
            Set<Integer> subtaskIds = epics.get(id).getSubTaskIds();
            int doneCounter = 0;
            for (int stId : subtaskIds)
                if (subTasks.get(stId).getStatus() == Status.DONE)
                    doneCounter++;
            if (subtaskIds.isEmpty() || subtaskIds.size() == doneCounter)
                epics.remove(id);
            else
                System.out.println("Ошибка: эпик не не может быть удалён, поскольку не завершён или содержит подзадачи");
        } else
            System.out.println("Ошибка: эпик с ID " + id + " не существует");
    }

    public void removeSubTask(int id) {
        if (!subTasks.isEmpty() && subTasks.get(id) != null) {
            SubTask st = subTasks.get(id);
            Epic e = epics.get(st.getEpicId());
            e.unbindSubTask(st);
            subTasks.remove(st.getId());
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

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public Task getEpicById(Integer id) {
        return epics.get(id);
    }

    public Task getSubTaskById(Integer id) {
        return subTasks.get(id);
    }

    public void updateTask(Task updated) {
        tasks.put(updated.getId(), updated);
    }

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

    public void updateSubTask(SubTask updated) {
        int updatedId = updated.getId();
        if (subTasks.containsKey(updatedId)) {
            subTasks.put(updatedId, updated);
            updateEpic(epics.get(updated.getEpicId()));
        } else
            System.out.println("Ошибка: подзадача ещё не создана");
    }

    public List<SubTask> getSubTasksForEpic(Epic e) {
        List<SubTask> result = new ArrayList<>();
        for (int stId : e.getSubTaskIds()) {
            SubTask st = subTasks.get(stId);
            if (st != null)
                result.add(st);
        }
        return result;
    }
}
