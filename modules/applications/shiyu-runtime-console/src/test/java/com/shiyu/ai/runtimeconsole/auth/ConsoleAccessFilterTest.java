package com.shiyu.ai.runtimeconsole.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** 覆盖控制台访问边界、会话校验和 CSRF 防护行为。 */
class ConsoleAccessFilterTest {

    @Test
    void rejectsNonLoopbackConsoleRequestsBeforeServingAnything() throws Exception {
        ConsoleAccessFilter filter = new ConsoleAccessFilter(new ConsoleSessionStore(java.time.Clock.systemUTC()));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/console/");
        request.setRemoteAddr("192.168.1.22");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};

        filter.doFilter(request, response, (req, res) -> reachedApplication[0] = true);

        assertEquals(403, response.getStatus());
        assertFalse(reachedApplication[0]);
    }

    @Test
    void rejectsMatrixParameterAliasesOfConsolePathsFromRemotePeers() throws Exception {
        ConsoleAccessFilter filter = new ConsoleAccessFilter(new ConsoleSessionStore(java.time.Clock.systemUTC()));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/console;alias=1/api/runtime");
        request.setRemoteAddr("192.168.1.22");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};

        filter.doFilter(request, response, (req, res) -> reachedApplication[0] = true);

        assertEquals(403, response.getStatus());
        assertFalse(reachedApplication[0]);
    }

    @Test
    void requiresAConsoleSessionForManagementApi() throws Exception {
        ConsoleAccessFilter filter = new ConsoleAccessFilter(new ConsoleSessionStore(java.time.Clock.systemUTC()));
        MockHttpServletRequest request = localRequest("GET", "/console/api/runtime");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};

        filter.doFilter(request, response, (req, res) -> reachedApplication[0] = true);

        assertEquals(401, response.getStatus());
        assertFalse(reachedApplication[0]);
    }

    @Test
    void requiresAConsoleSessionWhenMountedUnderServletContextPath() throws Exception {
        ConsoleAccessFilter filter = new ConsoleAccessFilter(new ConsoleSessionStore(java.time.Clock.systemUTC()));
        MockHttpServletRequest request = localRequest("GET", "/shiyu/console/api/runtime");
        request.setContextPath("/shiyu");
        request.setServletPath("/console/api/runtime");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};

        filter.doFilter(request, response, (req, res) -> reachedApplication[0] = true);

        assertEquals(401, response.getStatus());
        assertFalse(reachedApplication[0]);
    }

    @Test
    void requiresSameOriginAndCsrfForWrites() throws Exception {
        ConsoleSessionStore sessions = new ConsoleSessionStore(java.time.Clock.systemUTC());
        ConsoleSessionStore.Session session = sessions.exchange(sessions.issueOneTimeCode());
        ConsoleAccessFilter filter = new ConsoleAccessFilter(sessions);
        MockHttpServletRequest request = localRequest("POST", "/console/api/config");
        request.setCookies(new jakarta.servlet.http.Cookie(ConsoleAccessFilter.SESSION_COOKIE, session.sessionId()));
        request.addHeader("Origin", "http://127.0.0.1:9000");
        request.addHeader(ConsoleAccessFilter.CSRF_HEADER, session.csrfToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};

        filter.doFilter(request, response, (req, res) -> reachedApplication[0] = true);

        assertEquals(200, response.getStatus());
        assertTrue(reachedApplication[0]);
    }

    @Test
    void rejectsCrossOriginWriteEvenWithValidSessionAndCsrf() throws Exception {
        ConsoleSessionStore sessions = new ConsoleSessionStore(java.time.Clock.systemUTC());
        ConsoleSessionStore.Session session = sessions.exchange(sessions.issueOneTimeCode());
        ConsoleAccessFilter filter = new ConsoleAccessFilter(sessions);
        MockHttpServletRequest request = localRequest("POST", "/console/api/config");
        request.setCookies(new jakarta.servlet.http.Cookie(ConsoleAccessFilter.SESSION_COOKIE, session.sessionId()));
        request.addHeader("Origin", "http://evil.example");
        request.addHeader(ConsoleAccessFilter.CSRF_HEADER, session.csrfToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertEquals(403, response.getStatus());
    }

    @Test
    void allowsInternalShutdownOnlyWithTheLauncherSecret() throws Exception {
        ConsoleAccessFilter filter = new ConsoleAccessFilter(
                new ConsoleSessionStore(java.time.Clock.systemUTC()),
                new com.shiyu.ai.runtimeconsole.metrics.ConsoleHttpMetrics(
                        new io.micrometer.core.instrument.simple.SimpleMeterRegistry()),
                "launcher-secret");
        MockHttpServletRequest request = localRequest("POST", "/console/api/lifecycle/internal-shutdown");
        MockHttpServletResponse denied = new MockHttpServletResponse();
        filter.doFilter(request, denied, (req, res) -> { });
        assertEquals(401, denied.getStatus());

        request.addHeader("X-ShiYu-Launcher-Control", "launcher-secret");
        MockHttpServletResponse accepted = new MockHttpServletResponse();
        boolean[] reachedApplication = {false};
        filter.doFilter(request, accepted, (req, res) -> reachedApplication[0] = true);
        assertEquals(200, accepted.getStatus());
        assertTrue(reachedApplication[0]);
    }

    private static MockHttpServletRequest localRequest(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setScheme("http");
        request.setServerName("127.0.0.1");
        request.setServerPort(9000);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
