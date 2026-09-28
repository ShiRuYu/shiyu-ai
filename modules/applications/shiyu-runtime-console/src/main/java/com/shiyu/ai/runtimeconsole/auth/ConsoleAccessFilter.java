package com.shiyu.ai.runtimeconsole.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.shiyu.ai.runtimeconsole.metrics.ConsoleHttpMetrics;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.web.filter.OncePerRequestFilter;

/** 强制控制台只接受本机访问，并隔离会话与 CSRF 安全边界。 */
public final class ConsoleAccessFilter extends OncePerRequestFilter {

    public static final String SESSION_COOKIE = "SHIYU_CONSOLE_SESSION";
    public static final String CSRF_HEADER = "X-ShiYu-Console-CSRF";
    public static final String SESSION_ATTRIBUTE = ConsoleAccessFilter.class.getName() + ".session";
    private static final String EXCHANGE_PATH = "/console/api/session/exchange";
    private static final java.util.Set<String> LAUNCHER_INTERNAL_PATHS = java.util.Set.of(
            "/console/api/lifecycle/internal-shutdown", "/console/api/lifecycle/internal-link");

    private final ConsoleSessionStore sessions;
    private final ConsoleHttpMetrics httpMetrics;
    private final String launcherControlToken;

    public ConsoleAccessFilter(ConsoleSessionStore sessions) {
        this(sessions, new ConsoleHttpMetrics(new io.micrometer.core.instrument.simple.SimpleMeterRegistry()), System.getenv("SHIYU_LAUNCHER_CONTROL_TOKEN"));
    }

    public ConsoleAccessFilter(ConsoleSessionStore sessions, ConsoleHttpMetrics httpMetrics) {
        this(sessions, httpMetrics, System.getenv("SHIYU_LAUNCHER_CONTROL_TOKEN"));
    }

    public ConsoleAccessFilter(ConsoleSessionStore sessions, ConsoleHttpMetrics httpMetrics, String launcherControlToken) {
        this.sessions = sessions;
        this.httpMetrics = httpMetrics;
        this.launcherControlToken = launcherControlToken == null ? "" : launcherControlToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = applicationPath(request);
        if (!isConsolePath(path)) {
            long startedAt = System.nanoTime();
            boolean failed = false;
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException exception) {
                failed = true;
                throw exception;
            } finally {
                httpMetrics.record(
                        request.getMethod(),
                        path,
                        failed && response.getStatus() < 500 ? 500 : response.getStatus(),
                        System.nanoTime() - startedAt);
            }
            return;
        }
        if (!isLoopback(request.getRemoteAddr())) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "console is local-only");
            return;
        }

        if (LAUNCHER_INTERNAL_PATHS.contains(path)) {
            boolean expectedMethod = "/console/api/lifecycle/internal-link".equals(path)
                    ? "GET".equalsIgnoreCase(request.getMethod())
                    : "POST".equalsIgnoreCase(request.getMethod());
            if (!expectedMethod
                    || launcherControlToken.isBlank()
                    || !constantTimeEquals(launcherControlToken, request.getHeader("X-ShiYu-Launcher-Control"))) {
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "launcher control authentication required");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        boolean exchange = EXCHANGE_PATH.equals(path);
        boolean write = !isSafeMethod(request.getMethod());
        if (write && !isSameOrigin(request)) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "same-origin request required");
            return;
        }
        if (exchange) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsoleSessionStore.Session session = findSession(request);
        if (path.startsWith("/console/api/") && session == null) {
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "console session required");
            return;
        }
        if (write) {
            String csrfHeader = request.getHeader(CSRF_HEADER);
            if (session == null || !constantTimeEquals(session.csrfToken(), csrfHeader)) {
                reject(response, HttpServletResponse.SC_FORBIDDEN, "valid console CSRF token required");
                return;
            }
        }
        if (session != null) {
            request.setAttribute(SESSION_ATTRIBUTE, session);
        }
        filterChain.doFilter(request, response);
    }

    private ConsoleSessionStore.Session findSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE.equals(cookie.getName())) {
                return sessions.findSession(cookie.getValue());
            }
        }
        return null;
    }

    private static boolean isConsolePath(String path) {
        return "/console".equals(path) || path != null && path.startsWith("/console/");
    }

    private static String applicationPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (path == null) {
            return path;
        }
        if (contextPath != null
                && !contextPath.isEmpty()
                && path.startsWith(contextPath)
                && (path.length() == contextPath.length()
                        || path.charAt(contextPath.length()) == '/')) {
            path = path.substring(contextPath.length());
        }
        try {
            path = URI.create(path).getPath();
        } catch (IllegalArgumentException ignored) {
            // 编码无效的路径不得被提升为更宽松的路由。
        }
        return path.replaceAll(";[^/]*", "");
    }

    private static boolean isLoopback(String address) {
        if (address == null) {
            return false;
        }
        return "127.0.0.1".equals(address)
                || "::1".equals(address)
                || "0:0:0:0:0:0:0:1".equalsIgnoreCase(address)
                || "::ffff:127.0.0.1".equalsIgnoreCase(address);
    }

    private static boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private static boolean isSameOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank() || "null".equals(origin)) {
            return false;
        }
        try {
            URI uri = URI.create(origin);
            if (uri.getScheme() == null
                    || uri.getHost() == null
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
                return false;
            }
            int originPort = effectivePort(uri.getScheme(), uri.getPort());
            int requestPort = effectivePort(request.getScheme(), request.getServerPort());
            return uri.getScheme().equalsIgnoreCase(request.getScheme())
                    && uri.getHost().equalsIgnoreCase(request.getServerName())
                    && originPort == requestPort;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static int effectivePort(String scheme, int port) {
        if (port >= 0) {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return actual != null
                && MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        actual.getBytes(StandardCharsets.UTF_8));
    }

    private static void reject(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
