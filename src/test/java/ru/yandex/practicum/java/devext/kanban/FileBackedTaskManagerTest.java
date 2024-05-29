package ru.yandex.practicum.java.devext.kanban;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.FileBackedTaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("Менеджер задач in-memory с сохранением данных в файл")
public class FileBackedTaskManagerTest {

    private FileBackedTaskManager taskManager;

    @Test
    @DisplayName("Восстановление из файла резервной копии")
    void restoreFromBackup() {
        taskManager = new FileBackedTaskManager(Paths.get("src","test", "resources", "backup.csv"));
        assertAll(
                () -> assertEquals(2, taskManager.getEpics().size()),
                () -> assertEquals(2, taskManager.getTasks().size()),
                () -> assertEquals(2, taskManager.getSubTasks().size()),
                () -> assertEquals(2, taskManager.getEpicById(2).getSubTaskIds().size())
        );
    }

    @Nested
    @DisplayName("Операции с одиночными задачами")
    class SingleTaskTest {

        private Task task;
        private Epic epic;
        private SubTask subTask;
        private String taskCsv, epicCsv, subTaskCsv;
        private static final Path tmpBackupPath = Paths.get("src","test", "resources", "tmp_backup.csv");

        @BeforeEach
        void beforeEach() throws IOException {
            Files.deleteIfExists(tmpBackupPath);
            taskManager = new FileBackedTaskManager(tmpBackupPath);
            task = new Task(taskManager.getNextId(), "Test task");
            task.setDescription("Description");
            epic = new Epic(taskManager.getNextId(), "Test epic");
            epic.setDescription("Description");
            subTask = new SubTask(taskManager.getNextId(), "Test subtask");
            subTask.setDescription("Description");
            taskManager.addTask(task);
            taskManager.addEpic(epic);
            taskManager.addSubTask(subTask, epic);
            taskCsv = getCsvString(task);
            epicCsv = getCsvString(epic);
            subTaskCsv = getCsvString(subTask);
        }

        @AfterEach
        void afterEach() throws IOException {
            Files.deleteIfExists(tmpBackupPath);
        }

        @Test
        @DisplayName("Сохранение в файл при добавлении задач")
        void checkFileBackup() throws IOException {
            String backupContent = Files.readString(tmpBackupPath);
            assertAll(
                    () -> assertTrue(Files.exists(tmpBackupPath)),
                    () -> assertTrue(backupContent.contains(taskCsv)),
                    () -> assertTrue(backupContent.contains(subTaskCsv)),
                    () -> assertTrue(backupContent.contains(epicCsv))
            );
        }

        @Nested
        @DisplayName("Простая задача")
        class SimpleTaskTest {

            @Test
            @DisplayName("Обновление")
            void updateTask() throws IOException {
                task.setDescription(UUID.randomUUID().toString());
                taskManager.updateTask(task);
                taskCsv = getCsvString(task);
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertEquals(task, taskManager.getTaskById(task.getId())),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertTrue(backupContent.contains(taskCsv))
                );
            }

            @Test
            @DisplayName("Удаление")
            void removeTask() throws IOException {
                taskManager.removeTask(task.getId());
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertNull(taskManager.getTaskById(task.getId())),
                        () -> assertEquals(1, taskManager.getEpics().size()),
                        () -> assertEquals(1, taskManager.getSubTasks().size()),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertFalse(backupContent.contains(taskCsv))
                );
            }
        }

        @Nested
        @DisplayName("Эпик")
        class EpicTest {

            @Test
            @DisplayName("Обновление")
            void updateEpic() throws IOException {
                epic.setDescription(UUID.randomUUID().toString());
                taskManager.updateEpic(epic);
                epicCsv = getCsvString(epic);
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertEquals(epic, taskManager.getEpicById(epic.getId())),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertTrue(backupContent.contains(epicCsv))
                );
            }

            @Test
            @DisplayName("Удаление")
            void removeEpic() throws IOException {
                subTask.setStatus(Status.DONE);
                taskManager.removeEpic(epic.getId());
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertNull(taskManager.getEpicById(epic.getId())),
                        () -> assertEquals(1, taskManager.getTasks().size()),
                        () -> assertEquals(1, taskManager.getSubTasks().size()),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertFalse(backupContent.contains(epicCsv))
                );
            }
        }

        @Nested
        @DisplayName("Подзадача")
        class SubTaskTest {

            @Test
            @DisplayName("Обновление")
            void updateSubTask() throws IOException {
                subTask.setDescription(UUID.randomUUID().toString());
                taskManager.updateSubTask(subTask);
                subTaskCsv = getCsvString(subTask);
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertEquals(subTask.getDescription(), taskManager.getSubTaskById(subTask.getId()).getDescription()),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertTrue(backupContent.contains(subTaskCsv))
                );
            }

            @Test
            @DisplayName("Удаление")
            void removeSubTask() throws IOException {
                // TODO
                taskManager.removeSubTask(subTask.getId());
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertNull(taskManager.getSubTaskById(subTask.getId())),
                        () -> assertEquals(1, taskManager.getTasks().size()),
                        () -> assertEquals(1, taskManager.getEpics().size()),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertFalse(backupContent.contains(subTaskCsv))
                );
            }
        }
    }

    private static String getCsvString(Task t) {
        if (t instanceof SubTask st)
            return String.join(",",
                    String.valueOf(st.getId()),
                    st.getClass().getName(),
                    st.getName(),
                    st.getStatus().name(),
                    st.getDescription(),
                    String.valueOf(st.getEpicId()));
        else
            return String.join(",",
                    String.valueOf(t.getId()),
                    t.getClass().getName(),
                    t.getName(),
                    t.getStatus().name(),
                    t.getDescription(),
                    "");
    }
}
