package com.shiyu.ai.iam.implementation.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserMapper;
import com.shiyu.ai.iam.implementation.persistence.repository.SaTokenUserRepositoryImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 验证持久令牌消费的有效期、账户归属、单次撤销和并发更新保护。 */
class SaTokenConsumptionTest {
    private static final String TOKEN = "random-token-0123456789";
    private final UserMapper mapper = mock(UserMapper.class);
    private final SaTokenUserRepositoryImpl repository = new SaTokenUserRepositoryImpl();

    SaTokenConsumptionTest() {
        ReflectionTestUtils.setField(repository, "userMapper", mapper);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"short", "random-token-percent%", "random_token_012345", "random-token-back\\slash"})
    void invalidTokenShapesCannotReachTheDatabase(String token) {
        assertNull(repository.consumeToken(token));
        assertNull(repository.findUserIdByToken(token));
        verifyNoInteractions(mapper);
    }

    @ParameterizedTest
    @MethodSource("invalidTokenEntries")
    void expiredMalformedAndWrongAccountTokensAreNeverConsumed(String body) {
        UserDO user = row(body);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(user));
        when(mapper.selectOneById(anyLong())).thenReturn(user);
        assertNull(repository.consumeToken(TOKEN));
        verify(mapper, never()).updateByQuery(any(UserDO.class), any(QueryWrapper.class));
    }

    @Test
    void removesOnlyTheConsumedTerminalAndRetainsOtherLiveSessions() {
        SaSession account = new SaSession("account-42");
        account.setTerminalList(new ArrayList<>(List.of(
                new SaTerminalInfo().setTokenValue(TOKEN), new SaTerminalInfo().setTokenValue("other-token"))));
        Map<String, Object> ext = Map.of(
                "tokens", Map.of(TOKEN, validEntry(), "other-token", Map.of("loginId", "42")),
                "tokenSessions", Map.of(TOKEN, Map.of("data", "mine"), "other-token", Map.of("data", "keep")),
                "sessions", Map.of("42", Map.of("data", JSONUtils.toJsonString(account))));
        UserDO user = row(JSONUtils.toJsonString(ext));
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(user));
        when(mapper.selectOneById(anyLong())).thenReturn(user);
        when(mapper.updateByQuery(any(UserDO.class), any(QueryWrapper.class))).thenReturn(1);
        assertEquals(42L, repository.consumeToken(TOKEN));
        ArgumentCaptor<UserDO> updated = ArgumentCaptor.forClass(UserDO.class);
        verify(mapper).updateByQuery(updated.capture(), any(QueryWrapper.class));
        Map<String, Object> saved = JSONUtils.parseMap(updated.getValue().getExtInfo());
        assertEquals(Map.of("other-token", Map.of("loginId", "42")), saved.get("tokens"));
        assertFalse(((Map<?, ?>) saved.get("tokenSessions")).containsKey(TOKEN));
        Map<?, ?> session = (Map<?, ?>) ((Map<?, ?>) saved.get("sessions")).get("42");
        SaSession remaining = JSONUtils.parseObject((String) session.get("data"), SaSession.class);
        assertEquals(List.of("other-token"), remaining.terminalListCopy().stream().map(SaTerminalInfo::getTokenValue).toList());
    }

    @Test
    void removesAnEmptyAccountSessionAfterItsLastTokenIsConsumed() {
        SaSession account = new SaSession("account-42");
        account.setTerminalList(new ArrayList<>(List.of(new SaTerminalInfo().setTokenValue(TOKEN))));
        UserDO user = row(JSONUtils.toJsonString(Map.of(
                "tokens", Map.of(TOKEN, validEntry()),
                "sessions", Map.of("42", Map.of("data", JSONUtils.toJsonString(account)), "43", Map.of("data", "preserved")))));
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(user));
        when(mapper.selectOneById(anyLong())).thenReturn(user);
        when(mapper.updateByQuery(any(UserDO.class), any(QueryWrapper.class))).thenReturn(1);
        assertEquals(42L, repository.consumeToken(TOKEN));
        ArgumentCaptor<UserDO> updated = ArgumentCaptor.forClass(UserDO.class);
        verify(mapper).updateByQuery(updated.capture(), any(QueryWrapper.class));
        assertEquals(Map.of("43", Map.of("data", "preserved")), JSONUtils.parseMap(updated.getValue().getExtInfo()).get("sessions"));
    }

    @Test
    void retriesACasConflictWithoutOverwritingNewMetadata() {
        UserDO first = row(JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, validEntry()))));
        UserDO changed = row(JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, validEntry()), "newSetting", "preserve")));
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(first));
        when(mapper.selectOneById(anyLong())).thenReturn(first).thenReturn(changed);
        when(mapper.updateByQuery(any(UserDO.class), any(QueryWrapper.class))).thenReturn(0).thenReturn(1);
        assertEquals(42L, repository.consumeToken(TOKEN));
        ArgumentCaptor<UserDO> writes = ArgumentCaptor.forClass(UserDO.class);
        ArgumentCaptor<QueryWrapper> predicates = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper, times(2)).updateByQuery(writes.capture(), predicates.capture());
        assertCasPredicate(predicates.getAllValues().getFirst(), first.getExtInfo());
        assertCasPredicate(predicates.getAllValues().getLast(), changed.getExtInfo());
        var saved = JSONUtils.parseMap(writes.getAllValues().getLast().getExtInfo());
        assertEquals("preserve", saved.get("newSetting"));
        assertTrue(((Map<?, ?>) saved.get("tokens")).isEmpty());
    }

    @Test
    void missingOrStaleCandidatesAndCasExhaustionDoNotAuthenticate() {
        UserDO withoutId = new UserDO();
        UserDO missing = row(null);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(Arrays.asList(null, withoutId, missing));
        assertNull(repository.consumeToken(TOKEN));
        when(mapper.selectOneById(anyLong())).thenReturn(missing);
        assertNull(repository.consumeToken(TOKEN));
        UserDO valid = row(JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, validEntry()))));
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(valid));
        when(mapper.selectOneById(anyLong())).thenReturn(valid);
        when(mapper.updateByQuery(any(UserDO.class), any(QueryWrapper.class))).thenReturn(0);
        assertNull(repository.consumeToken(TOKEN));
        verify(mapper, times(8)).updateByQuery(any(UserDO.class), any(QueryWrapper.class));
        assertNull(repository.consumeToken("a".repeat(129)));
    }

    @Test
    void extInfoUpdatesUseCompareAndSwapWithOptionalPreviousValue() {
        assertFalse(repository.updateExtInfo(null, null));
        assertFalse(repository.updateExtInfo(new UserBO(), null));
        UserBO user = new UserBO();
        user.setId(42L);
        user.setExtInfo("{\"saved\":true}");
        when(mapper.updateByQuery(any(UserDO.class), any(QueryWrapper.class))).thenReturn(1).thenReturn(0);
        assertTrue(repository.updateExtInfo(user, null));
        user.setUpdateTime(new Date(1000));
        assertFalse(repository.updateExtInfo(user, "previous"));
        ArgumentCaptor<QueryWrapper> predicates = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper, times(2)).updateByQuery(any(UserDO.class), predicates.capture());
        assertCasPredicate(predicates.getAllValues().getFirst(), null);
        assertCasPredicate(predicates.getAllValues().getLast(), "previous");
    }

    private static void assertCasPredicate(QueryWrapper query, String previousValue) {
        String sql = query.toSQL();
        String normalized = sql.toLowerCase(java.util.Locale.ROOT)
                .replace("`", "").replace("\"", "").replaceAll("\\s+", "");
        assertTrue(normalized.contains("id=42"), sql);
        if (previousValue == null) {
            assertTrue(normalized.contains("ext_infoisnull"), sql);
        } else {
            assertTrue(normalized.contains("ext_info="), sql);
            assertTrue(sql.contains(previousValue), sql);
        }
    }

    private static Stream<String> invalidTokenEntries() {
        return Stream.of(
                "broken", "null", "{}", "{\"tokens\":[]}",
                JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, "invalid"))),
                JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, Map.of("expireTime", "later")))),
                JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, Map.of("expireTime", 0, "loginId", "42")))),
                JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, Map.of("expireTime", Long.MAX_VALUE, "loginId", 42)))),
                JSONUtils.toJsonString(Map.of("tokens", Map.of(TOKEN, Map.of("expireTime", Long.MAX_VALUE, "loginId", "43")))));
    }

    private static Map<String, Object> validEntry() {
        return Map.of("expireTime", Long.MAX_VALUE, "loginId", "42");
    }

    private static UserDO row(String extInfo) {
        UserDO user = new UserDO();
        user.setId(42L);
        user.setExtInfo(extInfo);
        return user;
    }
}
