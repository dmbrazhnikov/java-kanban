package ru.yandex.practicum.java.devext.kanban.task.management;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path backupFilePath;
    private static final String[] CSV_BACKUP_HEADER = {"id", "type", "name", "status", "description", "epic"};

    public FileBackedTaskManager() {
        super();
        backupFilePath = Paths.get(System.getProperty("backup.file.pathname"));
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
        List<String[]> records = new ArrayList<>();
        tasks.forEach((id, task) -> records.add(toCsvRecord(task)));
        epics.forEach((id, epic) -> records.add(toCsvRecord(epic)));
        subTasks.forEach((id, subTask) -> records.add(toCsvRecord(subTask)));
        try {
            Files.deleteIfExists(backupFilePath);
            // Path newBackupPath = Files.createFile(backupFilePath); //FIXME Удалить, если не нужно
            try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(backupFilePath.toFile())).withSeparator(',').build()) {
                writer.writeNext(CSV_BACKUP_HEADER);
                writer.writeAll(records);
            } catch (IOException e) {
                throw new ManagerSaveException();
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public String[] toCsvRecord(Task t) {
        if (t instanceof SubTask st)
            return new String[] {
                    String.valueOf(t.getId()),
                    t.getClass().getName(),
                    t.getName(),
                    t.getStatus().name(),
                    t.getDescription(),
                    String.valueOf(st.getEpicId())
            };
        else
            return new String[] {
                    String.valueOf(t.getId()),
                    t.getClass().getName(),
                    t.getName(),
                    t.getStatus().name(),
                    t.getDescription(),
                    null
            };
    }
}
