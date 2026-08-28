package com.shiyu.ai.auth.config;

import cn.dev33.satoken.session.SaSession;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.port.repository.SaTokenUserRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SaTokenDaoImplCoverageTest {
    @Test
    void persistsTokensSessionsAndObjectsAcrossCacheMisses() {
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        UserBO user = user(7L, "{}");
        when(repository.selectById(7L)).thenReturn(user);
        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString("7".getBytes(StandardCharsets.UTF_8)) + "_random";
        String tokenKey = "Authorization:login:token:" + token;

        dao.set(tokenKey, "7", 60);
        assertEquals("7", dao.get(tokenKey));
        assertTrue(dao.getTimeout(tokenKey) > 0);
        dao.update(tokenKey, "8");
        assertEquals("8", dao.get(tokenKey));
        dao.updateTimeout(tokenKey, 120);
        dao.delete(tokenKey);
        assertNull(dao.get(tokenKey));
        assertNull(dao.get("other:key"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getTimeout("other:key"));

        SaSession session = new SaSession("Authorization:login:session:7").set("user", "alice");
        dao.setSession(session, 60);
        assertNotNull(dao.getSession(session.getId()));
        assertTrue(dao.getSessionTimeout(session.getId()) > 0);
        dao.updateSession(session.set("role", "admin"));
        dao.updateSessionTimeout(session.getId(), 120);
        dao.deleteSession(session.getId());
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getSessionTimeout(session.getId()));

        dao.setObject("k", "value", 1);
        assertEquals("value", dao.getObject("k", String.class));
        assertNull(dao.getObject("k", Integer.class));
        dao.updateObject("k", 3);
        assertEquals(3, dao.getObject("k"));
        dao.deleteObject("k");
        assertNull(dao.getObject("k"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getObjectTimeout("k"));
        dao.updateObjectTimeout("k", 20);
        assertTrue(dao.searchData("", "", 0, 10, false).isEmpty());
        dao.destroy();
        verify(repository, atLeastOnce()).updateExtInfo(any(UserBO.class));
    }

    @Test
    void handlesMalformedAndExpiredEntriesWithoutOpeningAccess() {
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        UserBO user = user(7L, JSONUtils.toJsonString(Map.of(
                "tokens", Map.of("expired", Map.of("loginId", "7", "expireTime", 1L)),
                "sessions", Map.of("7", Map.of("data", "bad", "expireTime", 1L)))));
        when(repository.selectById(7L)).thenReturn(user);
        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);
        assertNull(dao.get("Authorization:login:token:expired"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getTimeout("Authorization:login:token:expired"));
        assertNull(dao.getSession("Authorization:login:session:7"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getSessionTimeout("Authorization:login:session:7"));
        assertNull(dao.get("Authorization:login:token:not-a-valid-token"));
        assertNull(dao.getSession("unknown"));
        dao.delete("Authorization:login:token:not-a-valid-token");
        dao.deleteSession("unknown");
        dao.destroy();
    }

    @Test
    void recoversTokenAndTokenSessionOwnershipWhenReverseCacheIsCold() {
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        UserBO user = user(7L, "{}");
        when(repository.selectById(7L)).thenReturn(user);
        String tokenValue = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("7".getBytes(StandardCharsets.UTF_8)) + "_random";
        String tokenKey = "Authorization:login:token:" + tokenValue;
        user.setExtInfo(JSONUtils.toJsonString(Map.of(
                "tokens", Map.of(tokenValue, Map.of("loginId", "7", "expireTime", Long.MAX_VALUE)))));

        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);
        assertEquals("7", dao.get(tokenKey));
        dao.update(tokenKey, "8");
        assertEquals("8", dao.get(tokenKey));
        dao.updateTimeout(tokenKey, 0);

        SaSession tokenSession = new SaSession("Authorization:login:token-session:" + tokenValue)
                .set("scope", "agent");
        dao.setSession(tokenSession, 60);
        SaTokenDaoImpl reader = new SaTokenDaoImpl(repository);
        assertNotNull(reader.getSession(tokenSession.getId()));
        assertTrue(reader.getSessionTimeout(tokenSession.getId()) > 0);
        reader.updateSession(tokenSession.set("scope", "knowledge"));
        reader.updateSessionTimeout(tokenSession.getId(), 0);
        reader.deleteSession(tokenSession.getId());
        reader.destroy();
        dao.destroy();
    }

    @Test
    void rejectsMalformedKeysAndHandlesMissingSectionsWithoutExceptions() {
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        when(repository.selectById(7L)).thenReturn(user(7L, "{}"));
        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);
        String malformed = "Authorization:login:token:not_base64_";
        assertNull(dao.get(malformed));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getTimeout(malformed));
        dao.set("other:key", "7", 1);
        dao.update("other:key", "7");
        dao.updateTimeout("other:key", 1);
        dao.delete("other:key");
        dao.setSession(new SaSession("unknown-session"), 1);
        dao.updateSession(new SaSession("unknown-session"));
        dao.updateSessionTimeout("unknown-session", 1);
        dao.deleteSession("unknown-session");
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getSessionTimeout("unknown-session"));
        dao.init();
        dao.destroy();
    }

    @Test
    void handlesMissingUsersSectionsAndSessionSerializationFallbacks() {
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        when(repository.selectById(anyLong())).thenReturn(null);
        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);

        assertNull(dao.get("Authorization:login:token:7_random"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE,
                dao.getTimeout("Authorization:login:token:7_random"));
        assertThrows(NumberFormatException.class,
                () -> dao.set("Authorization:login:token:7_random", "not-a-user", 1));

        dao.update("Authorization:login:token:7_random", "not-a-user");
        dao.updateTimeout("Authorization:login:token:7_random", 0);
        dao.delete("Authorization:login:token:7_random");
        assertNull(dao.getSession("Authorization:login:token-session:7"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE,
                dao.getSessionTimeout("Authorization:login:token-session:7"));
        dao.setSession(new SaSession("Authorization:login:token-session:7"), 1);
        dao.updateSession(new SaSession("Authorization:login:token-session:7"));
        dao.updateSessionTimeout("Authorization:login:token-session:7", 1);
        dao.deleteSession("Authorization:login:token-session:7");

        when(repository.selectById(7L)).thenReturn(user(7L,
                JSONUtils.toJsonString(Map.of("tokens", "wrong", "sessions", Map.of("7", Map.of()),
                        "tokenSessions", Map.of("7_random", Map.of("data", "not-json"))))));
        assertNull(dao.get("Authorization:login:token:7_random"));
        assertNull(dao.getSession("Authorization:login:token-session:7_random"));
        dao.destroy();
    }

    @Test
    void coversTokenParsingTimeoutAndMapHelperBoundaries() throws Exception {
        SaTokenDaoImpl dao = new SaTokenDaoImpl(mock(SaTokenUserRepository.class));
        Method parse = SaTokenDaoImpl.class.getDeclaredMethod("parseUserIdFromToken", String.class);
        parse.setAccessible(true);
        assertNull(parse.invoke(dao, new Object[]{null}));
        assertNull(parse.invoke(dao, ""));
        assertEquals(7L, parse.invoke(dao, "7"));
        assertNull(parse.invoke(dao, "not-a-number"));
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("7".getBytes(StandardCharsets.UTF_8));
        assertEquals(7L, parse.invoke(dao, encoded + "_random"));
        assertNull(parse.invoke(dao, "%%%_random"));

        Method key = SaTokenDaoImpl.class.getDeclaredMethod("extractSessionValueKey", String.class);
        key.setAccessible(true);
        assertEquals("7", key.invoke(dao, "Authorization:login:session:7"));
        assertEquals("token", key.invoke(dao, "Authorization:login:token-session:token"));
        assertNull(key.invoke(dao, "unknown"));

        Method cast = SaTokenDaoImpl.class.getDeclaredMethod("castMap", Object.class);
        cast.setAccessible(true);
        assertTrue(cast.invoke(dao, Map.of()) instanceof Map);
        assertNull(cast.invoke(dao, "wrong"));
        Method create = SaTokenDaoImpl.class.getDeclaredMethod("getOrCreateMap", Map.class, String.class);
        create.setAccessible(true);
        Map<String, Object> parent = new LinkedHashMap<>();
        assertTrue(create.invoke(dao, parent, "tokens") instanceof Map);
        assertSame(parent.get("tokens"), create.invoke(dao, parent, "tokens"));

        Method expired = SaTokenDaoImpl.class.getDeclaredMethod("isExpired", Map.class);
        expired.setAccessible(true);
        assertTrue((Boolean) expired.invoke(dao, Map.of("expireTime", 1L)));
        assertFalse((Boolean) expired.invoke(dao, Map.of("expireTime", Long.MAX_VALUE)));
        assertFalse((Boolean) expired.invoke(dao, Map.of("expireTime", "later")));
        Method remaining = SaTokenDaoImpl.class.getDeclaredMethod("getRemainingTimeout", Map.class);
        remaining.setAccessible(true);
        assertEquals(SaTokenDaoImpl.NEVER_EXPIRE, remaining.invoke(dao, Map.of("expireTime", Long.MAX_VALUE)));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, remaining.invoke(dao, Map.of("expireTime", 1L)));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, remaining.invoke(dao, Map.of("expireTime", "later")));

        Method cleanup = SaTokenDaoImpl.class.getDeclaredMethod("cleanupExpiredEntries", Map.class);
        cleanup.setAccessible(true);
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("tokens", new LinkedHashMap<>(Map.of("expired", Map.of("expireTime", 1L),
                "live", Map.of("expireTime", Long.MAX_VALUE))));
        cleanup.invoke(dao, ext);
        assertFalse(((Map<?, ?>) ext.get("tokens")).containsKey("expired"));
        dao.destroy();
    }

    private static UserBO user(Long id, String extInfo) {
        UserBO user = new UserBO();
        user.setId(id);
        user.setExtInfo(extInfo);
        return user;
    }
}
