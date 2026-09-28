package com.shiyu.ai.runtimeconsole.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** 将 URL 片段中的授权凭据交换为本地、非持久化的浏览器 Cookie。 */
@RestController
@RequestMapping("/console/api/session")
public final class ConsoleSessionController {

    private final ConsoleSessionStore sessions;

    public ConsoleSessionController(ConsoleSessionStore sessions) {
        this.sessions = sessions;
    }

    @PostMapping("/exchange")
    public SessionResponse exchange(
            @RequestBody ExchangeRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        ConsoleSessionStore.Session session = sessions.exchange(request.grant());
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Console link expired or already used");
        }
        servletResponse.addHeader(
                "Set-Cookie",
                ResponseCookie.from(ConsoleAccessFilter.SESSION_COOKIE, session.sessionId())
                        .httpOnly(true)
                        .secure(servletRequest.isSecure())
                        .sameSite("Strict")
                        .path(consoleCookiePath(servletRequest))
                        .build()
                        .toString());
        return new SessionResponse(true, session.csrfToken());
    }

    @GetMapping
    public SessionResponse status(HttpServletRequest request) {
        Object session = request.getAttribute(ConsoleAccessFilter.SESSION_ATTRIBUTE);
        if (!(session instanceof ConsoleSessionStore.Session current)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Console session required");
        }
        return new SessionResponse(true, current.csrfToken());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Object session = request.getAttribute(ConsoleAccessFilter.SESSION_ATTRIBUTE);
        if (session instanceof ConsoleSessionStore.Session current) {
            sessions.revoke(current.sessionId());
        }
        response.addHeader(
                "Set-Cookie",
                ResponseCookie.from(ConsoleAccessFilter.SESSION_COOKIE, "")
                        .httpOnly(true)
                        .secure(request.isSecure())
                        .sameSite("Strict")
                        .path(consoleCookiePath(request))
                        .maxAge(0)
                        .build()
                        .toString());
    }

    private static String consoleCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (contextPath == null ? "" : contextPath) + "/console";
    }

    /**
     * 封装控制台会话交换请求中的一次性授权片段。
     */
    public record ExchangeRequest(String grant) {}

    /** 表示控制台会话状态及其 CSRF 防护令牌。 */
    public record SessionResponse(boolean authenticated, String csrfToken) {}
}
