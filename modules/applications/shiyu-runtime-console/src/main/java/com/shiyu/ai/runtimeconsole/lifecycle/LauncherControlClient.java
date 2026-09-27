package com.shiyu.ai.runtimeconsole.lifecycle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Authenticated loopback client for the parent Windows launcher command channel. */
public final class LauncherControlClient {

    private final int port;
    private final String token;
    private final HttpClient httpClient;

    public LauncherControlClient(int port, String token) {
        this.port = port;
        this.token = token == null ? "" : token;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public static LauncherControlClient fromEnvironment() {
        String port = System.getenv("SHIYU_LAUNCHER_CONTROL_PORT");
        String token = System.getenv("SHIYU_LAUNCHER_CONTROL_TOKEN");
        if (port == null || token == null) {
            return new LauncherControlClient(-1, "");
        }
        try {
            return new LauncherControlClient(Integer.parseInt(port), token);
        } catch (NumberFormatException ignored) {
            return new LauncherControlClient(-1, "");
        }
    }

    public boolean available() {
        return port > 0 && port <= 65535 && !token.isBlank();
    }

    public void command(String action) {
        if (!available()) {
            throw new IllegalStateException("当前应用不是由 ShiYu Windows 启动器托管");
        }
        if (!"restart".equals(action) && !"stop".equals(action)) {
            throw new IllegalArgumentException("unsupported launcher command");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/command"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(action))
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 202) {
                throw new IllegalStateException("Windows 启动器拒绝生命周期请求（HTTP " + response.statusCode() + "）");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("无法连接本机 Windows 启动器", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 Windows 启动器响应时被中断", exception);
        }
    }

    /** Rejects accidental non-loopback use even if an invalid port was supplied. */
    static boolean isLoopbackAddress(InetSocketAddress address) {
        InetAddress inetAddress = address.getAddress();
        return inetAddress != null && inetAddress.isLoopbackAddress();
    }
}
