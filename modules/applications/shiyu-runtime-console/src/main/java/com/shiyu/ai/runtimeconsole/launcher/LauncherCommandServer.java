package com.shiyu.ai.runtimeconsole.launcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** A random-port, loopback-only authenticated command channel between launcher and backend. */
final class LauncherCommandServer implements AutoCloseable {

    private final HttpServer server;
    private final String token;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "shiyu-launcher-control");
        thread.setDaemon(true);
        return thread;
    });

    LauncherCommandServer(String token, Consumer<String> commandHandler) throws IOException {
        this.token = token;
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 8);
        server.createContext("/command", exchange -> handle(exchange, commandHandler));
        server.setExecutor(executor);
        server.start();
    }

    int port() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange, Consumer<String> commandHandler) throws IOException {
        if (!"127.0.0.1".equals(exchange.getRemoteAddress().getAddress().getHostAddress())
                || !"POST".equals(exchange.getRequestMethod())
                || !constantTimeEquals("Bearer " + token, exchange.getRequestHeaders().getFirst("Authorization"))) {
            respond(exchange, 403, "forbidden");
            return;
        }
        byte[] body = exchange.getRequestBody().readNBytes(32);
        if (body.length == 32 || exchange.getRequestBody().read() != -1) {
            respond(exchange, 413, "command too long");
            return;
        }
        String command = new String(body, StandardCharsets.US_ASCII).trim();
        if (!"restart".equals(command) && !"stop".equals(command)) {
            respond(exchange, 400, "unsupported command");
            return;
        }
        respond(exchange, 202, "accepted");
        executor.execute(() -> commandHandler.accept(command));
    }

    private static void respond(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return actual != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
