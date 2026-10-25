package com.shiyu.ai.iam.implementation.config;

import com.shiyu.ai.iam.implementation.infrastructure.auth.SaTokenDaoImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.SaTokenUserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 验证 Sa Token Dao Impl 相关功能、边界条件、异常路径和协作行为。
 */
class SaTokenDaoImplTest {
    private SaTokenUserRepository users;
    private SaTokenDaoImpl dao;
    private UserBO user;
    private final String key = "Authorization:login:token:NDI_test-token-value";

    @BeforeEach
    void setUp() {
        users = mock(SaTokenUserRepository.class);
        user = new UserBO();
        user.setId(42L);
        user.setExtInfo("{}");
        when(users.selectById(42L)).thenAnswer(invocation -> {
            UserBO snapshot = new UserBO();
            snapshot.setId(user.getId());
            snapshot.setExtInfo(user.getExtInfo());
            return snapshot;
        });
        when(users.updateExtInfo(any(UserBO.class), nullable(String.class)))
                .thenAnswer(invocation -> {
                    UserBO update = invocation.getArgument(0);
                    String expected = invocation.getArgument(1);
                    if (!java.util.Objects.equals(user.getExtInfo(), expected)) return false;
                    user.setExtInfo(update.getExtInfo());
                    return true;
                });
        dao = new SaTokenDaoImpl(users);
    }

    @AfterEach
    void tearDown() {
        dao.destroy();
    }

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
        verify(users, atLeastOnce()).updateExtInfo(any(UserBO.class), nullable(String.class));
    }

    @Test
    void validRandomTokenStillResolvesAfterDaoRestart() {
        String randomTokenKey = "Authorization:login:token:"
                + "bcf38261-60f9-4790-83bb-a5e33e917d17";
        dao.set(randomTokenKey, "42", 3600);
        dao.destroy();
        dao = new SaTokenDaoImpl(users);
        when(users.findUserIdByToken("bcf38261-60f9-4790-83bb-a5e33e917d17"))
                .thenReturn(42L);

        assertEquals("42", dao.get(randomTokenKey));
        assertTrue(dao.getTimeout(randomTokenKey) > 0);
        verify(users, times(2)).findUserIdByToken("bcf38261-60f9-4790-83bb-a5e33e917d17");
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
