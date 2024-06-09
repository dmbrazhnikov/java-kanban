package ru.yandex.practicum.java.devext.kanban.task.management;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path backupFilePath;
    private static final String[] CSV_BACKUP_HEADER = {"id", "type", "name", "status", "description", "epic"};

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;
        restoreFromBackup(backupFilePath);
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
        ICSVWriter writer = null;
        try {
            Files.deleteIfExists(backupFilePath);
            writer = new CSVWriterBuilder(new FileWriter(backupFilePath.toFile(), StandardCharsets.UTF_8))
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();
            writer.writeNext(CSV_BACKUP_HEADER);
            writer.writeAll(records);
        } catch (IOException e) {
            throw new ManagerSaveException();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            }
        }
    }

    private void restoreFromBackup(Path backupPath) {
        if (Files.exists(backupPath)) {
            if (!Files.isRegularFile(backupPath) || !Files.isReadable(backupPath))
                throw new ManagerLoadException("Backup file " + backupPath + " either is not a file or cannot be read");
            try {
                int maxId = 0;
                CSVParser parser = new CSVParserBuilder()
                        .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();
                CSVReader reader = new CSVReaderBuilder(new FileReader(backupPath.toFile(), StandardCharsets.UTF_8))
                        .withCSVParser(parser)
                        .withSkipLines(1)
                        .build();
                CSVIterator iterator = new CSVIterator(reader);
                while (iterator.hasNext()) {
                    String[] line = iterator.next();
                    int id = Integer.parseInt(line[0]);
                    if (id > maxId)
                        maxId = id;
                    String className = line[1];
                    Status status = Status.valueOf(line[3]);
                    if (className.equals("Epic")) {
                        Epic e = new Epic(id, line[2]);
                        e.setStatus(status);
                        e.setDescription(line[4]);
                        epics.put(id, e);
                    } else if (className.equals("SubTask")) {
                        SubTask st = new SubTask(id, line[2]);
                        st.setStatus(status);
                        st.setDescription(line[4]);
                        st.setEpicId(Integer.parseInt(line[5]));
                        subTasks.put(id, st);
                    } else {
                        Task t = new Task(id, line[2]);
                        t.setStatus(status);
                        t.setDescription(line[4]);
                        tasks.put(id, t);
                    }
                }
                for (SubTask st : subTasks.values()) {
                    Epic parentEpic = epics.get(st.getEpicId());
                    parentEpic.getSubTaskIds().add(st.getId());
                }
                idSeq = new AtomicInteger(maxId);
            } catch (FileNotFoundException e) {
                throw new ManagerLoadException("File not found: " + backupPath);
            } catch (IOException e) {
                throw new ManagerLoadException("IOException for " + backupPath);
            } catch (CsvValidationException e) {
                throw new ManagerLoadException("CsvValidationException for " + backupPath);
            }
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
