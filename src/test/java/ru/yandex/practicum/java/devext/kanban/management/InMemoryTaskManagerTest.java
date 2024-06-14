package ru.yandex.practicum.java.devext.kanban.management;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.devext.kanban.Managers;
import ru.yandex.practicum.java.devext.kanban.task.Status;
import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import ru.yandex.practicum.java.devext.kanban.task.management.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
            task = new Task(taskManager.getNextId(), "Test task");
            task.setStartDateTime(LocalDateTime.now().plusMinutes(ThreadLocalRandom.current().nextInt(10, 30)));
            task.setDuration(Duration.ofDays(2));
            epic = new Epic(taskManager.getNextId(), "Test epic");
            subTask = new SubTask(taskManager.getNextId(), "Test subtask");
            subTask.setStartDateTime(LocalDateTime.now().plusMinutes(ThreadLocalRandom.current().nextInt(10, 30)));
            subTask.setDuration(Duration.ofDays(1));
            taskManager.addTask(task);
            taskManager.addEpic(epic);
            taskManager.addSubTask(subTask, epic);
        }

        @Nested
        @DisplayName("Простая задача")
        class SimpleTaskTest {

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
                assertAll(
                        () -> assertNull(taskManager.getTaskById(task.getId())),
                        () -> assertEquals(1, taskManager.getEpics().size()),
                        () -> assertEquals(1, taskManager.getSubTasks().size())
                );
            }
        }

        @Nested
        @DisplayName("Эпик")
        class EpicTest {

            @Test
            @DisplayName("Получение")
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
                subTask.setStatus(Status.DONE);
                taskManager.removeEpic(epic.getId());
                assertAll(
                        () -> assertNull(taskManager.getEpicById(epic.getId())),
                        () -> assertEquals(1, taskManager.getTasks().size()),
                        () -> assertEquals(1, taskManager.getSubTasks().size())
                );
            }
        }

        @Nested
        @DisplayName("Подзадача")
        class SubTaskTest {

            @Test
            @DisplayName("Получение")
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
                assertAll(
                        () -> assertNull(taskManager.getSubTaskById(subTask.getId())),
                        () -> assertEquals(1, taskManager.getTasks().size()),
                        () -> assertEquals(1, taskManager.getEpics().size())
                );
            }
        }
    }

    @Nested
    @DisplayName("Связь эпика и подзадач")
    class EpicAndSubTaskRelationTest {

        private Epic epic;
        private Set<Integer> refSubTaskIds;
        private List<SubTask> refSubTasks;
        private static final int SUBTASKS_PER_EPIC = 3;

        @BeforeEach
        void beforeEach() {
            refSubTaskIds = new HashSet<>();
            refSubTasks = new LinkedList<>();
            epic = new Epic(taskManager.getNextId(), "Test epic");
            for (int i = 0; i < SUBTASKS_PER_EPIC; i++) {
                SubTask st = new SubTask(taskManager.getNextId(), "Test subtask " + (i + 1));
                st.setStartDateTime(LocalDateTime.now().plusMinutes(10));
                st.setDuration(Duration.ofDays(1));
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
            int stId = refSubTasks.get(0).getId();
            taskManager.removeSubTask(stId);
            assertFalse(epic.getSubTaskIds().contains(stId));
        }

        @Test
        @DisplayName("Зависимость длительности и даты-времени эпика от его подзадач")
        void setEpicTimeline() {
            Duration expectedEpicDuration = refSubTasks.stream()
                    .map(SubTask::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
            assertAll(
                    () -> assertEquals(refSubTasks.get(0).getStartDateTime(), epic.getStartDateTime()),
                    () -> assertEquals(refSubTasks.get(refSubTasks.size() - 1).getEndDateTime(), epic.getEndDateTime()),
                    () -> assertEquals(expectedEpicDuration, epic.getDuration())
            );
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
                    Task t = new Task(taskManager.getNextId(), "Test task " + (i + 1));
                    t.setStartDateTime(LocalDateTime.now().plusMinutes(ThreadLocalRandom.current().nextInt(10, 30)));
                    t.setDuration(Duration.ofDays(2));
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
                    Epic e = new Epic(taskManager.getNextId(), "Test epic " + (i + 1));
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
                    Epic e = new Epic(taskManager.getNextId(), "Test epic " + (i + 1));
                    refEpics.add(e);
                    taskManager.addEpic(e);
                    for (int j = 0; j < 3; j++) {
                        SubTask st = new SubTask(taskManager.getNextId(), "Subtask " + i + j);
                        st.setStartDateTime(LocalDateTime.now().plusMinutes(10));
                        st.setDuration(Duration.ofDays(1));
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

    @Nested
    @DisplayName("Приоритизация")
    class PrioritizationTest {

        private Task t1, t2, t3;
        private SubTask st1, st2, st3;
        private Epic e;


        @BeforeEach
        void beforeEach() {
            // Задачи для приоритизации
            t1 = new Task(taskManager.getNextId(), "Test task 3");
            t1.setStartDateTime(LocalDateTime.now().plusMinutes(60));
            t1.setDuration(Duration.ofDays(1));
            t2 = new Task(taskManager.getNextId(), "Test task 0");
            t2.setStartDateTime(LocalDateTime.now().minusMinutes(60));
            t2.setDuration(Duration.ofDays(1));
            e = new Epic(taskManager.getNextId(), "Test epic");
            taskManager.addEpic(e);
            st1 = new SubTask(taskManager.getNextId(), "Subtask 2");
            st1.setStartDateTime(LocalDateTime.now().plusMinutes(10));
            st1.setDuration(Duration.ofDays(1));
            st2 = new SubTask(taskManager.getNextId(), "Subtask 1");
            st2.setStartDateTime(LocalDateTime.now().minusMinutes(10));
            st2.setDuration(Duration.ofDays(1));
            // Задачи вне приоритизации
            t3 = new Task(taskManager.getNextId(), "Test task (no priority)");
            t3.setDuration(Duration.ofDays(1));
            st3 = new SubTask(taskManager.getNextId(), "Subtask (no priority)");
            st3.setDuration(Duration.ofHours(1));
        }

        @Test
        @DisplayName("Отсортированные по приоритету задачи")
        void getPrioritizedTasks() {
            Task[] refArray = new Task[4]; // Референс приоритезированных задач
            refArray[3] = t1;
            refArray[0] = t2;
            refArray[2] = st1;
            refArray[1] = st2;
            /* Выполнение */
            taskManager.addTask(t1);
            taskManager.addTask(t2);
            taskManager.addSubTask(st1, e);
            taskManager.addSubTask(st2, e);
            /* Проверка */
            LinkedList<Task> refList = new LinkedList<>();
            Collections.addAll(refList, refArray);
            LinkedList<Task> actualPrioritizedTaskList = taskManager.getPrioritizedTasks();
            assertEquals(refList, actualPrioritizedTaskList);
        }

        @Test
        @DisplayName("Задачи без срока начала")
        void skipPrioritizationForNullStartDateTime() {
            taskManager.addTask(t3);
            taskManager.addSubTask(st3, e);
            LinkedList<Task> actualPrioritizedTaskList = taskManager.getPrioritizedTasks();
            assertAll(
                    () -> assertFalse(actualPrioritizedTaskList.contains(t3)),
                    () -> assertFalse(actualPrioritizedTaskList.contains(st3)),
                    () -> assertTrue(taskManager.getTasks().contains(t3)),
                    () -> assertTrue(taskManager.getSubTasks().contains(st3))
            );
        }
    }
}