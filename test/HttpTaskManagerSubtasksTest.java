import com.google.gson.Gson;
import model.Epic;
import model.Subtask;
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

public class HttpTaskManagerSubtasksTest {

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
    public void getSubtasksEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void getSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    @Test
    public void getSubtask404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/subtasks/9999");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    public void getSubtaskById404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/9999");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    public void getSubtaskInvalidUrl404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/1/extra");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }

    //---------------------------------------------------
    //блок тестов методов для post
    //---------------------------------------------------
    @Test
    public void createSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    @Test
    public void createSubtask404() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        subtask.setId(2);
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = client.send(request1, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response2.statusCode());
        assertTrue(response2.body().contains(":)"));
    }

    @Test
    public void postTaskIncorrectPath404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/123/extra"); // length > 3

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
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask2", "desc",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        String json = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void updateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);

        subtask.setName("Updated");
        String json = gson.toJson(subtask);

        URI uri = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().isEmpty());
    }

    @Test
    public void updateSubtask404() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        subtask.setId(2);
        String json = gson.toJson(subtask);
        URI uri = URI.create("http://localhost:8080/subtasks/2");

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
    public void updateSubtask406() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask2", "desc",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("SubtaskUpd", "desc",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(10), epic.getId());
        subtask3.setId(2);
        String json = gson.toJson(subtask3);

        URI uri = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void postSubtask404() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Subtask2", "desc",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(10), epic.getId());
        String json = gson.toJson(subtask2);
        URI uri = URI.create("http://localhost:8080/subtasks/2");
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
    public void deleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("To Delete", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.createSubtask(subtask);

        URI uri = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(uri).DELETE().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertFalse(manager.getSubtasks().contains(subtask));
    }

    @Test
    public void deleteSubtask404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }

    @Test
    public void deleteNonExistentSubtask404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/subtasks/999");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().toLowerCase().contains("not found"));
    }
}
