package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getTasks()), 200);
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            try {
                Task task = manager.getTaskById(id);
                sendText(exchange, gson.toJson(task), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange, "Incorrect methode format");
        }
    }

    @Override
    void handlePost(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length != 2 && parts.length != 3) {
            sendNotFound(exchange, "Incorrect methode format");
        }
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(reader, Task.class);
        if (parts.length == 2) {
            try {
                manager.createTask(task);
                sendText(exchange, gson.toJson(manager.getTasks()), 201);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange, e.getMessage());
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            if (task.getId() != null && Integer.parseInt(parts[2]) == task.getId()) {
                try {
                    manager.updateTask(task);
                    sendText(exchange, gson.toJson(manager.getTasks()), 201);
                } catch (IllegalArgumentException e) {
                    sendHasInteractions(exchange, e.getMessage());
                } catch (NotFoundException e) {
                    sendNotFound(exchange, e.getMessage());
                }
            } else {
                sendNotFound(exchange, "Incorrect id format");
            }
        }
    }

    @Override
    void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length != 3) {
            sendNotFound(exchange, "Incorrect methode format. Id for task  " + parts[1] + " required");
        } else {
            int id = Integer.parseInt(parts[2]);
            try {
                Task deletedTask = manager.getTaskById(id);
                manager.deleteTaskById(deletedTask.getId());
                sendText(exchange, gson.toJson(manager.getTasks()), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }
}
