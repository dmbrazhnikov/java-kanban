package ru.yandex.practicum.java.devext.kanban.management;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.devext.kanban.BaseTest;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.filebacked.FileBackedTaskManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.practicum.java.devext.kanban.task.management.CommonDateTimeFormatter.ISO_LOCAL;


@DisplayName("Менеджер задач in-memory с сохранением данных в файл")
public class FileBackedTaskManagerTest extends BaseTest {

    private FileBackedTaskManager taskManager;

    @Test
    @DisplayName("Восстановление из файла резервной копии")
    void restoreFromBackup() {
        taskManager = new FileBackedTaskManager(Paths.get("src","test", "resources", "backup.csv"));
        assertAll(
                () -> assertEquals(2, taskManager.getEpics().size()),
                () -> assertEquals(2, taskManager.getTasks().size()),
                () -> assertEquals(3, taskManager.getSubTasks().size()),
                () -> assertEquals(3, taskManager.getEpicById(2).getSubTaskIds().size())
        );
    }

    @Nested
    @DisplayName("Операции с одиночными задачами")
    class SingleTaskTest {

        private Task task;
        private Epic epic;
        private String taskCsv, epicCsv;
        private List<Task> refSubTasks;
        private static final int SUBTASKS_PER_EPIC = 3;
        private static final Path tmpBackupPath = Paths.get("src","test", "resources", "tmp_backup.csv");

        @BeforeEach
        void beforeEach() throws IOException {
            Files.deleteIfExists(tmpBackupPath);
            taskManager = new FileBackedTaskManager(tmpBackupPath);
            // Простая задача
            task = new Task(taskManager.getNextId(), "Test task");
            task.setDescription("Description");
            task.setStartDateTime(LocalDateTime.now().plusMinutes(ThreadLocalRandom.current().nextInt(10, 30)));
            task.setDuration(Duration.ofDays(2));
            taskManager.addTask(task);
            taskCsv = getCsvString(task);
            // Эпик
            epic = new Epic(taskManager.getNextId(), "Test epic");
            epic.setDescription("Description");
            taskManager.addEpic(epic);
            epicCsv = getCsvString(epic);
            // Подзадача
            refSubTasks = new LinkedList<>();
            addSubtasksForEpic(refSubTasks, taskManager, epic, 3);
        }

        @AfterEach
        void afterEach() throws IOException {
            Files.deleteIfExists(tmpBackupPath);
        }

        @Test
        @DisplayName("Сохранение в файл при добавлении задач")
        void checkFileBackup() throws IOException {
            String backupContent = Files.readString(tmpBackupPath),
                    subTaskCsv = getCsvString(refSubTasks.get(0));
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
                        () -> assertEquals(refSubTasks.size(), taskManager.getSubTasks().size()),
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
                refSubTasks.forEach(st -> st.setStatus(Status.DONE));
                taskManager.removeEpic(epic.getId());
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertNull(taskManager.getEpicById(epic.getId())),
                        () -> assertEquals(1, taskManager.getTasks().size()),
                        () -> assertEquals(refSubTasks.size(), taskManager.getSubTasks().size()),
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
                SubTask st = (SubTask) refSubTasks.get(0);
                st.setDescription(UUID.randomUUID().toString());
                taskManager.updateSubTask(st);
                String subTaskCsv = getCsvString(st);
                String backupContent = Files.readString(tmpBackupPath);
                assertAll(
                        () -> assertEquals(st.getDescription(), taskManager.getSubTaskById(st.getId()).getDescription()),
                        () -> assertTrue(Files.exists(tmpBackupPath)),
                        () -> assertTrue(backupContent.contains(subTaskCsv))
                );
            }

            @Test
            @DisplayName("Удаление")
            void removeSubTask() throws IOException {
                SubTask st = (SubTask) refSubTasks.get(0);
                taskManager.removeSubTask(st.getId());
                String backupContent = Files.readString(tmpBackupPath),
                        subTaskCsv = getCsvString(st);
                assertAll(
                        () -> assertNull(taskManager.getSubTaskById(st.getId())),
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
                    st.getClass().getSimpleName(),
                    st.getName(),
                    st.getStatus().name(),
                    Optional.ofNullable(st.getDescription()).orElse(""),
                    String.valueOf(st.getEpicId()),
                    t.getStartDateTime().format(ISO_LOCAL.getDtf()),
                    String.valueOf(t.getDuration().toMinutes())
            );
        else
            return String.join(",",
                    String.valueOf(t.getId()),
                    t.getClass().getSimpleName(),
                    t.getName(),
                    t.getStatus().name(),
                    Optional.ofNullable(t.getDescription()).orElse(""),
                    "",
                    t instanceof Epic ? "" : t.getStartDateTime().format(ISO_LOCAL.getDtf()),
                    t instanceof Epic ? "" : String.valueOf(t.getDuration().toMinutes())
            );
    }
}
