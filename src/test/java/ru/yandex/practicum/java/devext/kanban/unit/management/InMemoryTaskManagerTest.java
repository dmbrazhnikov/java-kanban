package ru.yandex.practicum.java.devext.kanban.unit.management;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.devext.kanban.unit.BaseUnitTest;
import ru.yandex.practicum.java.devext.kanban.task.management.*;
import ru.yandex.practicum.java.devext.kanban.task.TaskStatus;
import ru.yandex.practicum.java.devext.kanban.task.Epic;
import ru.yandex.practicum.java.devext.kanban.task.SubTask;
import ru.yandex.practicum.java.devext.kanban.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Менеджер задач in-memory")
class InMemoryTaskManagerTest extends BaseUnitTest {

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
            task.setStartDateTime(LocalDateTime.now());
            task.setDuration(Duration.ofHours(1));
            epic = new Epic(taskManager.getNextId(), "Test epic");
            subTask = new SubTask(taskManager.getNextId(), "Test subtask");
            subTask.setStartDateTime(task.getEndDateTime().plusMinutes(10));
            subTask.setDuration(Duration.ofHours(1));
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
                        () -> assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task.getId())),
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
                subTask.setStatus(TaskStatus.DONE);
                taskManager.removeEpic(epic.getId());
                assertAll(
                        () -> assertThrows(NotFoundException.class, () -> taskManager.getEpicById(epic.getId())),
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
                        () -> assertThrows(NotFoundException.class, () -> taskManager.getSubTaskById(subTask.getId())),
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
        private List<Task> refSubTasks;

        @BeforeEach
        void beforeEach() {
            refSubTaskIds = new HashSet<>();
            refSubTasks = new LinkedList<>();
            epic = new Epic(taskManager.getNextId(), "Test epic");
            taskManager.addEpic(epic);
            addSubtasksForEpic(refSubTasks, taskManager, epic, 3);
            refSubTasks.forEach(st -> refSubTaskIds.add(st.getId()));
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
                    .map(t -> (SubTask) t)
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
        private List<Task> refEpics;
        private List<Task> refSubTasks;

        @Nested
        @DisplayName("Простая задача")
        class SimpleTaskTest {

            @BeforeEach
            void beforeEach() {
                refTasks = new ArrayList<>();
                addTasks(refTasks, taskManager, 3);
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
                addEpicWithSubTasks(refEpics, refSubTasks, taskManager, 2, 3);
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
                for (Task e : refEpics) {
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
            t1.setStartDateTime(LocalDateTime.now().plusHours(6));
            t1.setDuration(Duration.ofHours(1));
            t2 = new Task(taskManager.getNextId(), "Test task 0");
            t2.setStartDateTime(LocalDateTime.now().minusHours(6));
            t2.setDuration(Duration.ofHours(1));
            e = new Epic(taskManager.getNextId(), "Test epic");
            taskManager.addEpic(e);
            st1 = new SubTask(taskManager.getNextId(), "Subtask 2");
            st1.setStartDateTime(LocalDateTime.now().plusHours(3));
            st1.setDuration(Duration.ofHours(1));
            st2 = new SubTask(taskManager.getNextId(), "Subtask 1");
            st2.setStartDateTime(LocalDateTime.now().minusHours(3));
            st2.setDuration(Duration.ofHours(1));
            // Задачи вне приоритизации
            t3 = new Task(taskManager.getNextId(), "Test task (no priority)");
            t3.setDuration(Duration.ofHours(1));
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

    @Nested
    @DisplayName("Пересечение по сроку исполнения")
    class ExecutionDateTimeOverlapTest {

        private Task t1, t2;
        private SubTask st1;
        private Epic e;

        @BeforeEach
        void beforeEach() {
            t1 = new Task(taskManager.getNextId(), "Test task 1");
            t1.setStartDateTime(LocalDateTime.now());
            t1.setDuration(Duration.ofHours(1));
            st1 = new SubTask(taskManager.getNextId(), "Test subtask 1");
            st1.setStartDateTime(t1.getStartDateTime().plusMinutes(10));
            st1.setDuration(Duration.ofHours(1));
            t2 = new Task(taskManager.getNextId(), "Test task 2");
            t2.setStartDateTime(st1.getStartDateTime().plusMinutes(10));
            t2.setDuration(Duration.ofHours(1));
            e = new Epic(taskManager.getNextId(), "Test epic");
            taskManager.addEpic(e);
        }

        @Test
        @DisplayName("Первая начинается раньше второй")
        void firstStartsEarlierThanSecond() {
            taskManager.addTask(t1);
            assertThrows(ExecutionDateTimeOverlapException.class, () -> taskManager.addSubTask(st1, e));
        }

        @Test
        @DisplayName("Вторая начинается раньше первой")
        void secondStartsEarlierThanFirst() {
            taskManager.addSubTask(st1, e);
            assertThrows(ExecutionDateTimeOverlapException.class, () -> taskManager.addTask(t2));
        }
    }
}