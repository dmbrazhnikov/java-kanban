package ru.yandex.practicum.java.devext.kanban.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.java.devext.kanban.HttpTaskServer;
import ru.yandex.practicum.java.devext.kanban.task.*;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@DisplayName("REST API")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class RestApiTest {

    private static Gson gson;
    private static final String baseUrl = "http://localhost:8080";
    private static HttpClient client;
    private static final HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString();
    HttpResponse.BodyHandler<Void> voidBodyHandler = HttpResponse.BodyHandlers.discarding();

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls();
        gson = gsonBuilder.create();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        HttpTaskServer.main(new String[]{});
    }

    @AfterEach
    void afterEach() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl + "/stop"))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofPublisher());
    }


    @Nested
    @Order(1)
    @DisplayName("Добавление всех видов задач")
    @Description("Эти тесты должны быть выполнены в первую очередь, поскольку это фундамент для всех остальных")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Add {

        @Test
        @Order(1)
        @DisplayName("Простая задача")
        void addTask() throws IOException, InterruptedException {
            Task task = new Task("Test task");
            task.setDuration(Duration.ofHours(1));
            task.setDescription("Description");
            String taskJson = gson.toJson(task);
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .uri(URI.create(baseUrl + "/tasks"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(201, response.statusCode()),
                    () -> assertTrue(gson.fromJson(response.body(), Task.class).getId() > -1)
            );
        }

        @Test
        @Order(2)
        @DisplayName("Эпик")
        void addEpic() throws IOException, InterruptedException {
            Epic epic = new Epic("Test epic");
            epic.setDescription("Description");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                    .uri(URI.create(baseUrl + "/epics"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(201, response.statusCode()),
                    () -> assertTrue(gson.fromJson(response.body(), Epic.class).getId() > -1)
            );
        }

        @Test
        @Order(3)
        @DisplayName("Подзадача")
        void addSubTask() throws IOException, InterruptedException {
            Epic epic = new Epic("Test epic");
            epic.setDescription("Description");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                    .uri(URI.create(baseUrl + "/epics"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            epic = gson.fromJson(response.body(), Epic.class);
            SubTask subTask = new SubTask("Test subtask");
            subTask.setDuration(Duration.ofHours(1));
            subTask.setDescription("Description");
            subTask.setEpicId(epic.getId());
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> responseSubTask = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(201, responseSubTask.statusCode()),
                    () -> assertTrue(gson.fromJson(responseSubTask.body(), SubTask.class).getId() > -1)
            );
        }
    }


    @Nested
    @Order(2)
    @DisplayName("Простая задача")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SimpleTaskTest {

        private static Task baseTask;

        @BeforeEach
        void beforeEach() throws IOException, InterruptedException {
            baseTask = new Task("Test task");
            baseTask.setDuration(Duration.ofHours(1));
            baseTask.setDescription("Description");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(baseTask)))
                    .uri(URI.create(baseUrl + "/tasks"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            baseTask = gson.fromJson(response.body(), Task.class);
        }

        @Test
        @Order(1)
        @DisplayName("Получение по ID")
        void getById() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/tasks/" + baseTask.getId()))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(200, response.statusCode()),
                    () -> assertEquals(gson.toJson(baseTask), response.body())
            );
        }

        @Test
        @Order(2)
        @DisplayName("Обновление")
        void update() throws IOException, InterruptedException {
            baseTask.setDescription("Updated");
            baseTask.setDuration(baseTask.getDuration().plusMinutes(10));
            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(baseTask)))
                    .uri(URI.create(baseUrl + "/tasks/" + baseTask.getId()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(201, response.statusCode());
        }

        @Test
        @Order(3)
        @DisplayName("Добавление непересекающейся")
        void addNonOverlapping() throws IOException, InterruptedException {
            Task nonOverlappingTask = new Task("Non-overlapping task");
            nonOverlappingTask.setStartDateTime(baseTask.getEndDateTime().plusMinutes(30));
            nonOverlappingTask.setDuration(Duration.ofHours(1));
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nonOverlappingTask)))
                    .uri(URI.create(baseUrl + "/tasks"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(201, response.statusCode());
        }

        @Test
        @Order(4)
        @DisplayName("Получение всех")
        void getAll() throws IOException, InterruptedException {
            // Подготовка
            List<Task> refTasks = new ArrayList<>();
            Task nonOverlappingTask = new Task("Non-overlapping task");
            nonOverlappingTask.setStartDateTime(baseTask.getEndDateTime().plusMinutes(30));
            nonOverlappingTask.setDuration(Duration.ofHours(1));
            refTasks.add(baseTask);
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nonOverlappingTask)))
                    .uri(URI.create(baseUrl + "/tasks"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> prepRs = client.send(request, stringBodyHandler);
            nonOverlappingTask = gson.fromJson(prepRs.body(), Task.class);
            refTasks.add(nonOverlappingTask);
            // Выполнение
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/tasks"))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> testRs = client.send(request, stringBodyHandler);
            List<Task> actualTasks = gson.fromJson(testRs.body(),  new TaskListTypeToken().getType());
            // Проверка
            assertAll(
                    () -> assertEquals(200, testRs.statusCode()),
                    () -> assertEquals(refTasks, actualTasks)
            );
        }

        @Test
        @Order(5)
        @DisplayName("Удаление")
        void remove() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(baseUrl + "/tasks/" + baseTask.getId()))
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(200, response.statusCode());
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Эпик")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EpicTest {

        private static Epic baseEpic;

        @BeforeEach
        void beforeEach() throws IOException, InterruptedException {
            baseEpic = addBaseEpic();
        }

        @Test
        @Order(1)
        @DisplayName("Получение по ID")
        void getById() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/epics/" + baseEpic.getId()))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(200, response.statusCode()),
                    () -> assertEquals(gson.toJson(baseEpic), response.body())
            );
        }

        @Test
        @Order(2)
        @DisplayName("Получение всех")
        void getAll() throws IOException, InterruptedException {
            // Подготовка
            List<Epic> refEpics = new ArrayList<>();
            Epic anotherEpic = new Epic("Another epic");
            anotherEpic.setDescription("Another epic");
            refEpics.add(baseEpic);
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(anotherEpic)))
                    .uri(URI.create(baseUrl + "/epics"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> prepRs = client.send(request, stringBodyHandler);
            anotherEpic = gson.fromJson(prepRs.body(), Epic.class);
            refEpics.add(anotherEpic);
            // Выполнение
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/epics"))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> testRs = client.send(request, stringBodyHandler);
            List<Epic> actualTasks = gson.fromJson(testRs.body(), new EpicListTypeToken().getType());
            // Проверка
            assertAll(
                    () -> assertEquals(200, testRs.statusCode()),
                    () -> assertEquals(refEpics, actualTasks)
            );
        }

        @Test
        @Order(3)
        @DisplayName("Удаление")
        void remove() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(baseUrl + "/epics/" + baseEpic.getId()))
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(200, response.statusCode());
        }
    }


    @Nested
    @Order(3)
    @DisplayName("Подзадача")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SubTaskTest {

        private static SubTask baseSubTask;

        @BeforeEach
        void beforeEach() throws IOException, InterruptedException {
            Epic baseEpic = addBaseEpic();
            baseSubTask = new SubTask("Test subtask");
            baseSubTask.setDuration(Duration.ofHours(1));
            baseSubTask.setDescription("Description");
            baseSubTask.setEpicId(baseEpic.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(baseSubTask)))
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            baseSubTask = gson.fromJson(response.body(), SubTask.class);
        }

        @Test
        @Order(1)
        @DisplayName("Получение по ID")
        void getById() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/subtasks/" + baseSubTask.getId()))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, stringBodyHandler);
            assertAll(
                    () -> assertEquals(200, response.statusCode()),
                    () -> assertEquals(gson.toJson(baseSubTask), response.body())
            );
        }

        @Test
        @Order(2)
        @DisplayName("Обновление")
        void update() throws IOException, InterruptedException {
            baseSubTask.setDescription("Updated");
            baseSubTask.setDuration(baseSubTask.getDuration().plusMinutes(10));
            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(baseSubTask)))
                    .uri(URI.create(baseUrl + "/subtasks/" + baseSubTask.getId()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(201, response.statusCode());
        }

        @Test
        @Order(3)
        @DisplayName("Добавление непересекающейся")
        void addNonOverlapping() throws IOException, InterruptedException {
            SubTask nonOverlappingSubTask = new SubTask("Non-overlapping subtask");
            nonOverlappingSubTask.setStartDateTime(baseSubTask.getEndDateTime().plusMinutes(30));
            nonOverlappingSubTask.setDuration(Duration.ofHours(1));
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nonOverlappingSubTask)))
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(201, response.statusCode());
        }

        @Test
        @Order(4)
        @DisplayName("Получение всех")
        void getAllTasks() throws IOException, InterruptedException {
            // Подготовка
            List<SubTask> refSubTasks = new ArrayList<>();
            refSubTasks.add(baseSubTask);
            SubTask nonOverlapping = new SubTask("Non-overlapping subtask");
            nonOverlapping.setStartDateTime(baseSubTask.getEndDateTime().plusMinutes(30));
            nonOverlapping.setDuration(Duration.ofHours(1));
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nonOverlapping)))
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> prepRs = client.send(request, stringBodyHandler);
            nonOverlapping = gson.fromJson(prepRs.body(), SubTask.class);
            refSubTasks.add(nonOverlapping);
            // Выполнение
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> testRs = client.send(request, stringBodyHandler);
            List<Task> actualSubTasks = gson.fromJson(testRs.body(),  new SubTaskListTypeToken().getType());
            // Проверка
            assertAll(
                    () -> assertEquals(200, testRs.statusCode()),
                    () -> assertEquals(refSubTasks, actualSubTasks)
            );
        }

        @Test
        @Order(5)
        @DisplayName("Удаление")
        void remove() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(baseUrl + "/subtasks/" + baseSubTask.getId()))
                    .build();
            HttpResponse<Void> response = client.send(request, voidBodyHandler);
            assertEquals(200, response.statusCode());
        }
    }

    @ParameterizedTest
    @DisplayName("Запрос несуществующих")
    @ValueSource(strings = {"/tasks", "/subtasks", "/epics"})
    void requestNonExistent(String taskType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl + taskType + "/" + 99999))
                .header("Accept", "application/json")
                .build();
        HttpResponse<Void> response = client.send(request, voidBodyHandler);
        assertEquals(404, response.statusCode());
    }

    private Epic addBaseEpic() throws IOException, InterruptedException {
        Epic baseEpic = new Epic("Test epic");
        baseEpic.setDescription("Description");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(baseEpic)))
                .uri(URI.create(baseUrl + "/epics"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, stringBodyHandler);
        return gson.fromJson(response.body(), Epic.class);
    }
}
