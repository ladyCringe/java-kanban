package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())
                && exchange.getRequestURI().getPath().split("/").length == 2) {
            sendText(exchange, gson.toJson(manager.getHistory()), 200);
        } else {
            sendNotFound(exchange, "Not a valid request for history");
        }
    }
}
