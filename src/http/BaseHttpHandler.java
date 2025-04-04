package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] parts = exchange.getRequestURI().getPath().split("/");

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange, parts);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange, "Method not allowed");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Throwable e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        sendNotFound(exchange, "Method not found");
    }

    void handlePost(HttpExchange exchange, String[] parts) throws IOException {
        sendNotFound(exchange, "Method not found");
    }

    void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        sendNotFound(exchange, "Method not found");
    }

    protected void sendText(HttpExchange h, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendText(h, text, 404);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendText(h, text, 406);
    }

    protected void sendServerError(HttpExchange h, String text) throws IOException {
        sendText(h, text, 500);
    }
}