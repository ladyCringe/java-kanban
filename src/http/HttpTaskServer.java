package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager manager;
    private final HttpServer server;
    private final Gson gson;

    public HttpTaskServer(TaskManager newManager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.manager = newManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(manager, gson));
        server.createContext("/epics", new EpicsHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на порту " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("HTTP-сервер остановлен");
        server.stop(0);
    }

    public Gson getGson() {
        return gson;
    }


    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
        httpTaskServer.start();
    }
}
