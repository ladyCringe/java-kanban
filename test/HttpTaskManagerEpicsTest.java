import com.google.gson.Gson;
import model.Epic;
import model.Subtask;
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

public class HttpTaskManagerEpicsTest {

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
    public void getEpicsEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for get", "desc");
        manager.createEpic(epic);

        int id = epic.getId();

        URI uri = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic for get"));
    }

    @Test
    public void getEpicByInvalidId() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/epics/9999");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with subtasks", "desc");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15), epic.getId());
        manager.createSubtask(sub);
        Subtask sub2 = new Subtask("Sub2", "desc2", TaskStatus.NEW,
                LocalDateTime.now().plus(Duration.ofMinutes(30)), Duration.ofMinutes(15), epic.getId());
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Sub"));
    }

    @Test
    public void getEpicSubtasks404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/9999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    public void getEpicSubtasksIncorrectPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/1/subtasks/extra"); // parts.length > 4
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Incorrect methode format"));
    }


    //---------------------------------------------------
    //блок тестов методов для post
    //---------------------------------------------------
    @Test
    public void createEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic Name", "Epic Description");
        String json = gson.toJson(epic);

        URI uri = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Epic Name"));
    }

    @Test
    public void updateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);
        epic.setName("Updated Epic");
        int id = epic.getId();

        URI uri = URI.create("http://localhost:8080/epics/" + id);
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Updated Epic"));
    }

    @Test
    public void updateEpic404() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Update", "descriptionUpdate");
        epic1.setId(1);
        String json = gson.toJson(epic1);
        URI uri = URI.create("http://localhost:8080/epics/1");

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
    public void createEpic404() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Test Task", "Description");
        epic1.setId(1);
        String taskJson = gson.toJson(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics");
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
    public void postEpicIncorrectPath404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/123/extra"); // length > 3

        String json = gson.toJson(new Epic("Bad", "Invalid path"));
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
    public void postEpic404() throws IOException, InterruptedException {
        Epic epic = new Epic("task", "description");
        manager.createTask(epic);

        Epic epic1 = new Epic("Update", "descriptionUpdate");
        String json = gson.toJson(epic1);
        URI uri = URI.create("http://localhost:8080/epics/1");
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
    public void deleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Delete Me", "desc");
        manager.createEpic(epic);
        int id = epic.getId();

        URI uri = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).DELETE().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    public void deleteEpic404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).DELETE().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void deleteNonExistentEpic404() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/epics/999");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().toLowerCase().contains("not found")); // сообщение об ошибке
    }
}