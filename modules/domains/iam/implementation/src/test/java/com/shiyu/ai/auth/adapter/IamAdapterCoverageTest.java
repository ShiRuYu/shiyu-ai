package com.shiyu.ai.auth.adapter;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamAdapterCoverageTest {
    @Test
    void resolvesTokensAndSessionContextWithoutOpeningOnFrameworkErrors() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            stp.when(() -> StpUtil.getLoginIdByToken("valid")).thenReturn("7");
            assertEquals(7L, helper.getUserIdByToken("valid"));
            stp.when(() -> StpUtil.getLoginIdByToken("missing")).thenReturn(null);
            assertNull(helper.getUserIdByToken("missing"));
            stp.when(() -> StpUtil.getLoginIdByToken("broken"))
                    .thenThrow(new IllegalStateException("expired"));
            assertNull(helper.getUserIdByToken("broken"));

            stp.when(StpUtil::getTokenTimeout).thenReturn(30L);
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getTokenValue).thenReturn("token");
            assertEquals(30L, helper.getTokenTimeout());
            assertTrue(helper.isFrameworkLogin());
            assertEquals("token", helper.login(7L));
            helper.logout(7L);
            assertEquals("token", helper.refreshToken(7L));

            SaSession session = mock(SaSession.class);
            stp.when(StpUtil::getSession).thenReturn(session);
            UserContext context = mock(UserContext.class);
            SaTokenHelper.saveUserContextToSession(context);
            stp.verify(() -> StpUtil.getSession(), atLeastOnce());
            stp.when(() -> session.get("userContext")).thenReturn(context);
            assertSame(context, SaTokenHelper.getUserContextFromSession());
            SaTokenHelper.clearUserContextSession();
            verify(session).delete("userContext");
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
        UserScopeRoleRepository repository = new UserScopeRoleRepository() {
            @Override public List<UserScopeRoleBO> selectByUserId(Long userId) {
                return List.of(new UserScopeRoleBO());
            }
            @Override public void insert(UserScopeRoleBO value) { }
            @Override public void deleteByUserIdAndTenantId(Long userId, TenantId tenantId) { }
            @Override public void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, TenantId tenantId) { }
        };
        assertTrue(repository.selectByUserIds(null).isEmpty());
        assertEquals(2, repository.selectByUserIds(List.of(1L, 2L)).size());
    }
}
