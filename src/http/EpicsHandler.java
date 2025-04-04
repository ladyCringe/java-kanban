package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Epic;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getEpics()), 200);
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);

            try {
                Epic epic = manager.getEpicById(id);
                sendText(exchange, gson.toJson(epic), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else if (parts.length == 4) {
            try {
                sendText(exchange, gson.toJson(manager.getEpicById(Integer.parseInt(parts[2])).getSubtasks()), 200);
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
        Epic epic = gson.fromJson(reader, Epic.class);

        if (parts.length == 2) {
            try {
                manager.createEpic(epic);
                sendText(exchange, gson.toJson(manager.getEpicById(epic.getId())), 201);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            if (epic.getId() != null && Integer.parseInt(parts[2]) == epic.getId()) {
                try {
                    manager.updateEpic(epic);
                    sendText(exchange, 201);
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
            sendNotFound(exchange, "Incorrect methode format. Id for epic  " + parts[1] + " is required");
        } else {
            int id = Integer.parseInt(parts[2]);
            try {
                Epic deletedEpic = manager.getEpicById(id);
                manager.deleteEpicById(deletedEpic.getId());
                sendText(exchange, gson.toJson(manager.getEpics()), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }
}
