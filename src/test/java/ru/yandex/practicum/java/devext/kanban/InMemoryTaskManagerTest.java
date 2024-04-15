package ru.yandex.practicum.java.devext.kanban;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Менеджер задач in-memory")
class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    @DisplayName("Класс менеджера задач по умолчанию")
    void isInstantiatedWithNew() {
        assertInstanceOf(InMemoryTaskManager.class, taskManager);
    }

    @Nested
    @DisplayName("Операции с одиночными задачами")
    class SingleTaskTest {

        private Task task;
        private Epic epic;
        private SubTask subTask;

        @BeforeEach
        void beforeEach() {
            task = new Task("Test task");
            epic = new Epic("Test epic");
            subTask = new SubTask("Test subtask");
        }

        @Nested
        @DisplayName("Простая задача")
        class SimpleTaskTest {

            @BeforeEach
            void beforeEach() {
                taskManager.addTask(task);
            }

            @Test
            @DisplayName("Добавление и получение")
            void getTaskById() {
                assertEquals(task, taskManager.getTaskById(task.getId()));
            }

            @Test
            @DisplayName("Обновление")
            void updateTask() {
                task.setDescription(UUID.randomUUID().toString());
                taskManager.updateTask(task);
                assertEquals(task, taskManager.getTaskById(task.getId()));
            }

            @Test
            @DisplayName("Удаление")
            void removeTask() {
                taskManager.removeTask(task.getId());
                assertNull(taskManager.getTaskById(task.getId()));
            }
        }

        @Nested
        @DisplayName("Эпик")
        class EpicTest {

            @BeforeEach
            void beforeEach() {
                taskManager.addEpic(epic);
            }

            @Test
            @DisplayName("Добавление и получение")
            void getEpicById() {
                assertEquals(epic, taskManager.getEpicById(epic.getId()));
            }

            @Test
            @DisplayName("Обновление")
            void updateEpic() {
                epic.setDescription(UUID.randomUUID().toString());
                taskManager.updateEpic(epic);
                assertEquals(epic, taskManager.getEpicById(epic.getId()));
            }

            @Test
            @DisplayName("Удаление")
            void removeEpic() {
                taskManager.removeEpic(epic.getId());
                assertNull(taskManager.getEpicById(epic.getId()));
            }
        }

        @Nested
        @DisplayName("Подзадача")
        class SubTaskTest {

            @BeforeEach
            void beforeEach() {
                taskManager.addEpic(epic);
                taskManager.addSubTask(subTask, epic);
            }

            @Test
            @DisplayName("Добавление и получение")
            void getSubTaskById() {
                SubTask actualSubTask = taskManager.getSubTaskById(subTask.getId());
                assertAll(
                        () -> assertEquals(subTask, actualSubTask),
                        () -> assertEquals(epic.getId(), actualSubTask.getEpicId())
                );
            }

            @Test
            @DisplayName("Обновление")
            void updateSubTask() {
                subTask.setDescription(UUID.randomUUID().toString());
                taskManager.updateSubTask(subTask);
                assertEquals(subTask.getDescription(), taskManager.getSubTaskById(subTask.getId()).getDescription());
            }

            @Test
            @DisplayName("Удаление")
            void removeSubTask() {
                taskManager.removeSubTask(subTask.getId());
                assertNull(taskManager.getSubTaskById(subTask.getId()));
            }
        }
    }

    @Nested
    @DisplayName("Связь эпика и подзадач")
    class EpicAndSubTaskRelationTest {

        private Epic epic;
        private Set<Integer> refSubTaskIds;
        private List<SubTask> refSubTasks;

        @BeforeEach
        void beforeEach() {
            refSubTaskIds = new HashSet<>();
            refSubTasks = new ArrayList<>();
            epic = new Epic("Test epic");
            for (int i = 0; i < 3; i++) {
                SubTask st = new SubTask("Test subtask " + (i + 1));
                refSubTaskIds.add(st.getId());
                refSubTasks.add(st);
                taskManager.addSubTask(st, epic);
            }
        }

        @Test
        @DisplayName("Единый ID эпика у всех его подзадач")
        void epicIdIsConsistentInSubTasks() {
            Set<Integer> epicIds = new HashSet<>();
            for (Integer i : refSubTaskIds) {
                SubTask st = taskManager.getSubTaskById(i);
                epicIds.add(st.getEpicId());
            }
            assertEquals(epicIds, Set.of(epic.getId()));
        }

        @Test
        @DisplayName("Корректные ID подзадач в их эпике")
        void correctSubTaskIdsInEpic() {
            assertEquals(refSubTaskIds, epic.getSubTaskIds());
        }

        @Test
        @DisplayName("Получение списка подазадач для эпика")
        void getSubTasksForEpic() {
            List<SubTask> actualSubTasks = taskManager.getSubTasksForEpic(epic);
            assertAll(
                    () -> assertEquals(refSubTasks.size(), actualSubTasks.size()),
                    () -> assertTrue(actualSubTasks.containsAll(refSubTasks))
            );
        }

        @Test
        @DisplayName("Отвязывание ID подзадачи при её удалении")
        void unbindSubTaskFromEpic() {
            int stId = refSubTasks.getFirst().getId();
            taskManager.removeSubTask(stId);
            assertFalse(epic.getSubTaskIds().contains(stId));
        }
    }

    @Nested
    @DisplayName("Операции с множествами задач")
    class MultipleTasksTest {

        private List<Task> refTasks;
        private List<Epic> refEpics;
        private List<SubTask> refSubTasks;

        @Nested
        @DisplayName("Простая задача")
        class SimpleTaskTest {

            @BeforeEach
            void beforeEach() {
                refTasks = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Task t = new Task("Test task " + (i + 1));
                    refTasks.add(t);
                    taskManager.addTask(t);
                }
            }

            @Test
            @DisplayName("Получение всех")
            void getAllTasks() {
                List<Task> actualTasks = taskManager.getTasks();
                assertAll(
                        () -> assertEquals(refTasks.size(), actualTasks.size()),
                        () -> assertTrue(actualTasks.containsAll(refTasks))
                );
            }

            @Test
            @DisplayName("Удаление всех")
            void removeAllTasks() {
                taskManager.removeAllTasks();
                assertEquals(0, taskManager.getTasks().size());
            }
        }

        @Nested
        @DisplayName("Эпик")
        class EpicTest {

            @BeforeEach
            void beforeEach() {
                refEpics = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Epic e = new Epic("Test epic " + (i + 1));
                    refEpics.add(e);
                    taskManager.addEpic(e);
                }
            }

            @Test
            @DisplayName("Получение всех")
            void getAllEpics() {
                List<Epic> actualEpics = taskManager.getEpics();
                assertAll(
                        () -> assertEquals(refEpics.size(), actualEpics.size()),
                        () -> assertTrue(actualEpics.containsAll(refEpics))
                );
            }

            @Test
            @DisplayName("Удаление всех")
            void removeAllTasks() {
                taskManager.removeAllEpics();
                assertEquals(0, taskManager.getEpics().size());
            }
        }

        @Nested
        @DisplayName("Подзадача")
        class SubTaskTest {

            @BeforeEach
            void beforeEach() {
                refEpics = new ArrayList<>();
                refSubTasks = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Epic e = new Epic("Test epic " + (i + 1));
                    refEpics.add(e);
                    taskManager.addEpic(e);
                    for (int j = 0; j < 3; j++) {
                        SubTask st = new SubTask("Subtask " + i + j);
                        refSubTasks.add(st);
                        taskManager.addSubTask(st, e);
                    }
                }
            }

            @Test
            @DisplayName("Получение всех")
            void getSubTasks() {
                List<SubTask> actualSubTasks = taskManager.getSubTasks();
                assertAll(
                        () -> assertEquals(refSubTasks.size(), actualSubTasks.size()),
                        () -> assertTrue(actualSubTasks.containsAll(refSubTasks))
                );
            }

            @Test
            @DisplayName("Удаление всех")
            void removeAllSubTasks() {
                taskManager.removeAllSubTasks();
                int counter = 0;
                for (Epic e : refEpics) {
                    Epic actualEpic = taskManager.getEpicById(e.getId());
                    counter += actualEpic.getSubTaskIds().size();
                }
                int finalCounter = counter;
                assertAll(
                        () -> assertEquals(0, taskManager.getSubTasks().size()),
                        () -> assertEquals(0, finalCounter)
                );
            }
        }
    }
}