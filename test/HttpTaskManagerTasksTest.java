import com.google.gson.Gson;
import exceptions.NotFoundException;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;
import http.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;

    @BeforeEach
    public void beforeEach() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = server.getGson();

        server.start();
    }

    @AfterEach
    public void afterEach() {
        server.stop();
    }

    //---------------------------------------------------
    //блок тестов методов для get
    //---------------------------------------------------
    @Test
    public void getTasksEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        Task task = new Task("Single Task", "Test",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);

        int id = task.getId();

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Single Task"));
    }

    @Test
    public void getTask404() throws IOException, InterruptedException, NotFoundException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/9999");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getTaskById404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/9999"); // task with id 9999 doesn't exist
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    public void getTaskInvalidUrl404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/1/extra");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }

    //---------------------------------------------------
    //блок тестов методов для post
    //---------------------------------------------------
    @Test
    public void createTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void createTask404() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description",
                TaskStatus.NEW);
        task.setId(1);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = client.send(request1, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response2.statusCode());
        assertTrue(response2.body().contains(":)"));
    }

    @Test
    public void postTaskIncorrectPath404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/123/extra"); // length > 3

        String json = gson.toJson(new Task("Bad", "Invalid path",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }

    @Test
    public void postTask406() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("A", "desc",
                TaskStatus.NEW, now, Duration.ofMinutes(30));
        Task task2 = new Task("B", "overlap",
                TaskStatus.NEW, now.plusMinutes(15), Duration.ofMinutes(30));

        manager.createTask(task1);
        String json = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        Task task = new Task("task", "description",
                TaskStatus.NEW);
        manager.createTask(task);

        Task task1 = new Task("Update", "descriptionUpdate",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        task1.setId(1);
        String json = gson.toJson(task1);
        URI uri = URI.create("http://localhost:8080/tasks/1");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    @Test
    public void updateTask404() throws IOException, InterruptedException {
        Task task1 = new Task("Update", "descriptionUpdate",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        task1.setId(1);
        String json = gson.toJson(task1);
        URI uri = URI.create("http://localhost:8080/tasks/1");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    public void updateTask406() throws IOException, InterruptedException {
        Task task = new Task("task", "description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);
        Task task2 = new Task("task", "description",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(30));
        manager.createTask(task2);

        Task task1 = new Task("Update", "descriptionUpdate",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(10));
        task1.setId(1);
        String json = gson.toJson(task1);
        URI uri = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void postTask404() throws IOException, InterruptedException {
        Task task = new Task("task", "description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);

        Task task1 = new Task("Update", "descriptionUpdate",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(10));
        String json = gson.toJson(task1);
        URI uri = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    //---------------------------------------------------
    //блок тестов методов для delete
    //---------------------------------------------------
    @Test
    public void deleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Delete Me", "Test",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);

        int id = task.getId();

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertFalse(manager.getTasks().contains(task));
    }

    @Test
    public void deleteTask404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }

    @Test
    public void deleteNonExistentTask404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/tasks/999");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().toLowerCase().contains("not found"));
    }
}
