package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {

    TaskManager manager;
    Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getSubtasks()), 200);
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            try {
                Subtask subtask = manager.getSubtaskById(id);
                sendText(exchange, gson.toJson(subtask), 200);
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
        Subtask subtask = gson.fromJson(reader, Subtask.class);
        if (parts.length == 2) {
            try {
                manager.createSubtask(subtask);
                sendText(exchange, gson.toJson(manager.getSubtasks()), 201);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange, e.getMessage());
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            if (subtask.getId() != null && Integer.parseInt(parts[2]) == subtask.getId()) {
                try {
                    manager.updateSubtask(subtask);
                    sendText(exchange, gson.toJson(manager.getSubtasks()), 201);
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
            sendNotFound(exchange, "Incorrect methode format. Id for subtask  " + parts[1] + " required");
        } else {
            int id = Integer.parseInt(parts[2]);
            try {
                Subtask deletedSubtask = manager.getSubtaskById(id);
                manager.deleteSubtaskById(deletedSubtask.getId());
                sendText(exchange, gson.toJson(manager.getSubtasks()), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }
}
