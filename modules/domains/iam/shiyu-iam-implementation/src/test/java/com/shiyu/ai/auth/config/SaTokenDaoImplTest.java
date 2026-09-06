package com.shiyu.ai.auth.config;

import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.port.repository.SaTokenUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SaTokenDaoImplTest {
    private SaTokenUserRepository users;
    private SaTokenDaoImpl dao;
    private UserBO user;
    private final String key = "Authorization:login:token:test-token";

    @BeforeEach
    void setUp() {
        users = mock(SaTokenUserRepository.class);
        user = new UserBO(); user.setId(42L); user.setExtInfo("{}");
        when(users.selectById(42L)).thenReturn(user);
        dao = new SaTokenDaoImpl(users);
    }

    @AfterEach
    void tearDown() { dao.destroy(); }

    @Test
    void storesReadsUpdatesAndDeletesTokenWithTenantIndependentUserRepository() {
        assertNull(dao.get("other:key"));
        dao.set(key, "42", 60);
        assertEquals("42", dao.get(key));
        assertTrue(dao.getTimeout(key) > 0);
        dao.update(key, "43");
        assertEquals("43", dao.get(key));
        dao.updateTimeout(key, 120);
        assertTrue(dao.getTimeout(key) > 0);
        dao.delete(key);
        assertNull(dao.get(key));
        verify(users, atLeastOnce()).updateExtInfo(user);
    }

    @Test
    void handlesObjectCacheAndMalformedKeysWithoutThrowing() {
        dao.setObject("object", "value", 10);
        assertEquals("value", dao.getObject("object"));
        assertEquals("value", dao.getObject("object", String.class));
        assertNull(dao.getObject("object", Long.class));
        dao.updateObject("object", 7L);
        assertEquals(7L, dao.getObject("object"));
        dao.deleteObject("object");
        assertNull(dao.getObject("object"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getObjectTimeout("object"));
        assertEquals(SaTokenDaoImpl.NOT_VALUE_EXPIRE, dao.getTimeout("bad"));
        dao.update("bad", "not-a-user");
        dao.delete("bad");
        assertTrue(dao.searchData("", "", 0, 10, false).isEmpty());
    }
}
