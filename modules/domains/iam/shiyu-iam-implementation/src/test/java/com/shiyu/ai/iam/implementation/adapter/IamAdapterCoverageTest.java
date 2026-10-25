package com.shiyu.ai.iam.implementation.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaRequestForMock;
import cn.dev33.satoken.context.mock.SaResponseForMock;
import cn.dev33.satoken.context.mock.SaStorageForMock;
import com.shiyu.ai.iam.implementation.infrastructure.auth.SaTokenDaoImpl;
import com.shiyu.ai.iam.implementation.port.repository.SaTokenUserRepository;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

import com.shiyu.ai.common.foundation.context.model.UserContext;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

/**
 * 验证 Iam Adapter Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class IamAdapterCoverageTest {
    @Test
    void tokenLogoutAndRefreshLeaveOtherUserSessionsValid() {
        var previousConfig = SaManager.getConfig();
        var previousDao = SaManager.getSaTokenDao();
        var previousContext = SaManager.getSaTokenContext();
        var previousLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new cn.dev33.satoken.stp.StpLogic("login"));
        SaTokenUserRepository repository = mock(SaTokenUserRepository.class);
        java.util.Map<String, Object> persisted = new java.util.HashMap<>();
        when(repository.selectById(7L)).thenAnswer(invocation -> {
            var user = new com.shiyu.ai.iam.implementation.domain.model.UserBO();
            user.setId(7L);
            user.setExtInfo((String) persisted.getOrDefault("extInfo", "{}"));
            return user;
        });
        when(repository.findUserIdByToken(anyString())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            var ext = com.shiyu.ai.common.foundation.utils.JSONUtils.parseMap(
                    (String) persisted.getOrDefault("extInfo", "{}"));
            Object tokens = ext == null ? null : ext.get("tokens");
            return tokens instanceof java.util.Map<?, ?> tokenMap && tokenMap.containsKey(value)
                    ? 7L
                    : null;
        });
        when(repository.updateExtInfo(any(), nullable(String.class))).thenAnswer(invocation -> {
            var update = invocation.getArgument(0, com.shiyu.ai.iam.implementation.domain.model.UserBO.class);
            String expected = invocation.getArgument(1);
            String current = (String) persisted.getOrDefault("extInfo", "{}");
            if (!java.util.Objects.equals(current, expected)) return false;
            persisted.put("extInfo", update.getExtInfo());
            return true;
        });
        when(repository.consumeToken(anyString())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            String current = (String) persisted.getOrDefault("extInfo", "{}");
            var ext = com.shiyu.ai.common.foundation.utils.JSONUtils.parseMap(current);
            if (ext == null || !(ext.get("tokens") instanceof java.util.Map<?, ?> tokens)) return null;
            Object tokenEntry = tokens.get(value);
            if (!(tokenEntry instanceof java.util.Map<?, ?> tokenMap)) return null;
            Object expiry = tokenMap.get("expireTime");
            if (!(expiry instanceof Number number)
                    || (number.longValue() != Long.MAX_VALUE
                            && number.longValue() <= System.currentTimeMillis())) return null;
            @SuppressWarnings("unchecked")
            var mutableTokens = (java.util.Map<String, Object>) tokens;
            mutableTokens.remove(value);
            if (ext.get("tokenSessions") instanceof java.util.Map<?, ?> sessions) {
                @SuppressWarnings("unchecked")
                var mutableSessions = (java.util.Map<String, Object>) sessions;
                mutableSessions.remove(value);
            }
            persisted.put("extInfo", com.shiyu.ai.common.foundation.utils.JSONUtils.toJsonString(ext));
            return 7L;
        });
        SaTokenDaoImpl dao = new SaTokenDaoImpl(repository);
        SaManager.setConfig(
                new SaTokenConfig().setTokenName("Authorization").setTokenStyle("uuid"));
        SaManager.setSaTokenDao(dao);
        SaTokenContextForThreadLocal context = new SaTokenContextForThreadLocal();
        SaManager.setSaTokenContext(context);
        SaTokenHelper helper = SaTokenHelper.getInstance();
        try {
            context.setContext(
                    new SaRequestForMock(), new SaResponseForMock(), new SaStorageForMock());
            SaManager.getConfig().setIsConcurrent(true).setIsShare(false);
            String tokenA = helper.login(7L);
            String tokenB = helper.login(7L);
            assertNotEquals(tokenA, tokenB);
            assertEquals(7L, helper.getUserIdByToken(tokenA));
            String replacement = helper.refreshToken(tokenA);
            assertNotNull(replacement);
            assertNull(helper.getUserIdByToken(tokenA));
            assertEquals(7L, helper.getUserIdByToken(tokenB));
            assertEquals(7L, helper.getUserIdByToken(replacement));

            helper.logoutToken(tokenB);
            assertNull(helper.getUserIdByToken(tokenB));
            assertEquals(7L, helper.getUserIdByToken(replacement));
            assertNull(helper.refreshToken(tokenB));
        } finally {
            context.clearContext();
            dao.destroy();
            SaManager.setConfig(previousConfig);
            SaManager.setSaTokenDao(previousDao);
            SaManager.setSaTokenContext(previousContext);
            StpUtil.setStpLogic(previousLogic);
        }
    }

    @Test
    void resolvesTokensAndSessionContextWithoutOpeningOnFrameworkErrors() {
        var previousDao = SaManager.getSaTokenDao();
        var atomicDao =
                mock(
                        cn.dev33.satoken.dao.SaTokenDao.class,
                        withSettings().extraInterfaces(
                                com.shiyu.ai.iam.implementation.infrastructure.auth.AtomicTokenConsumer.class));
        when(((com.shiyu.ai.iam.implementation.infrastructure.auth.AtomicTokenConsumer) atomicDao)
                        .consumeToken("valid"))
                .thenReturn(7L);
        SaManager.setSaTokenDao(atomicDao);
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            stp.when(() -> StpUtil.getLoginIdByToken("valid")).thenReturn("7");
            stp.when(() -> StpUtil.getLoginIdByToken("other")).thenReturn("7");
            assertEquals(7L, helper.getUserIdByToken("valid"));
            stp.when(() -> StpUtil.getLoginIdByToken("missing")).thenReturn(null);
            assertNull(helper.getUserIdByToken("missing"));
            stp.when(() -> StpUtil.getLoginIdByToken("broken"))
                    .thenThrow(new IllegalStateException("expired"));
            assertNull(helper.getUserIdByToken("broken"));

            stp.when(StpUtil::getTokenTimeout).thenReturn(30L);
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getTokenValue).thenReturn("token");
            stp.when(() -> StpUtil.getTokenSessionByToken("valid"))
                    .thenReturn(mock(SaSession.class));
            assertEquals(30L, helper.getTokenTimeout());
            assertTrue(helper.isFrameworkLogin());
            assertEquals("token", helper.login(7L));
            helper.logout(7L);
            helper.logoutToken("valid");
            stp.verify(() -> StpUtil.logoutByTokenValue("valid"));
            stp.verify(() -> StpUtil.logout(7L));
            assertEquals("token", helper.refreshToken(7L));
            assertEquals("token", helper.refreshToken("valid"));
            stp.verify(() -> StpUtil.logoutByTokenValue("valid"), times(1));

            SaSession session = mock(SaSession.class);
            stp.when(StpUtil::getSession).thenReturn(session);
            UserContext context = mock(UserContext.class);
            SaTokenHelper.saveUserContextToSession(context);
            stp.verify(() -> StpUtil.getSession(), atLeastOnce());
            stp.when(() -> session.get("userContext")).thenReturn(context);
            assertSame(context, SaTokenHelper.getUserContextFromSession());
            SaTokenHelper.clearUserContextSession();
            verify(session).delete("userContext");
        } finally {
            SaManager.setSaTokenDao(previousDao);
        }
    }

    @Test
    void handlesUnavailableSessionAsUnauthenticated() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getSession).thenThrow(new IllegalStateException("session expired"));
            assertNull(SaTokenHelper.getUserContextFromSession());
            assertDoesNotThrow(SaTokenHelper::clearUserContextSession);
        }
    }

    @Test
    void flattensScopedRoleQueriesAndRejectsNullInputSafely() {
        UserScopeRoleRepository repository =
                new UserScopeRoleRepository() {
                    @Override
                    public List<UserScopeRoleBO> selectByUserId(Long userId) {
                        return List.of(new UserScopeRoleBO());
                    }

                    @Override
                    public void insert(UserScopeRoleBO value) {}

                    @Override
                    public void deleteByUserIdAndTenantId(Long userId, TenantId tenantId) {}

                    @Override
                    public void deleteByUserIdRoleIdAndTenantId(
                            Long userId, Long roleId, TenantId tenantId) {}
                };
        assertTrue(repository.selectByUserIds(null).isEmpty());
        assertEquals(2, repository.selectByUserIds(List.of(1L, 2L)).size());
    }
}
