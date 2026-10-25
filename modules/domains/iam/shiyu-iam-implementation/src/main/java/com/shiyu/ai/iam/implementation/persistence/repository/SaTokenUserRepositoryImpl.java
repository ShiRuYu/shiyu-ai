package com.shiyu.ai.iam.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserMapper;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 负责 Sa Token 用户 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class SaTokenUserRepositoryImpl
        implements com.shiyu.ai.iam.implementation.port.repository.SaTokenUserRepository {

    /**
     * 用户映射器，表示当前对象中的对应属性。
     */
    @Resource private UserMapper userMapper;

    public UserBO selectById(Long userId) {
        return MapstructUtils.convert(userMapper.selectOneById(userId), UserBO.class);
    }

    @Override
    public Long findUserIdByToken(String tokenValue) {
        if (tokenValue == null || tokenValue.length() < 16 || tokenValue.length() > 128
                || tokenValue.indexOf('%') >= 0 || tokenValue.indexOf('_') >= 0
                || tokenValue.indexOf('\\') >= 0) {
            return null;
        }
        QueryWrapper candidates = QueryWrapper.create().where(UserDO::getExtInfo).like(tokenValue);
        for (UserDO user : userMapper.selectListByQuery(candidates)) {
            if (user == null || user.getId() == null || user.getExtInfo() == null) continue;
            try {
                Map<String, Object> ext = JSONUtils.parseMap(user.getExtInfo());
                if (ext != null && ext.get("tokens") instanceof Map<?, ?> tokens
                        && tokens.containsKey(tokenValue)) {
                    return user.getId();
                }
            } catch (RuntimeException ignored) {
                // 损坏的旧版 extInfo 不能用于令牌认证。
            }
        }
        return null;
    }

    @Override
    public boolean updateExtInfo(UserBO user, String expectedExtInfo) {
        if (user == null || user.getId() == null) return false;
        UserDO update = new UserDO();
        update.setExtInfo(user.getExtInfo());
        update.setUpdateTime(
                user.getUpdateTime() == null
                        ? LocalDateTime.now()
                        : user.getUpdateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime());
        QueryWrapper query = QueryWrapper.create().where(UserDO::getId).eq(user.getId());
        if (expectedExtInfo == null) {
            query.and(UserDO::getExtInfo).isNull();
        } else {
            query.and(UserDO::getExtInfo).eq(expectedExtInfo);
        }
        return userMapper.updateByQuery(update, query) == 1;
    }

    @Override
    public Long consumeToken(String tokenValue) {
        if (tokenValue == null || tokenValue.length() < 16 || tokenValue.length() > 128
                || tokenValue.indexOf('%') >= 0 || tokenValue.indexOf('_') >= 0
                || tokenValue.indexOf('\\') >= 0) {
            return null;
        }
        QueryWrapper candidates = QueryWrapper.create().where(UserDO::getExtInfo).like(tokenValue);
        for (UserDO candidate : userMapper.selectListByQuery(candidates)) {
            if (candidate == null || candidate.getId() == null) continue;
            Long userId = candidate.getId();
            for (int attempt = 0; attempt < 8; attempt++) {
                UserDO current = userMapper.selectOneById(userId);
                if (current == null || current.getExtInfo() == null) break;
                String expectedExtInfo = current.getExtInfo();
                Map<String, Object> ext;
                try {
                    ext = JSONUtils.parseMap(expectedExtInfo);
                } catch (RuntimeException ignored) {
                    break;
                }
                if (ext == null || !(ext.get("tokens") instanceof Map<?, ?> rawTokens)) break;
                Object rawEntry = rawTokens.get(tokenValue);
                if (!(rawEntry instanceof Map<?, ?> rawToken)) break;
                Object expireValue = rawToken.get("expireTime");
                if (!(expireValue instanceof Number expiry)) return null;
                if (expiry.longValue() != Long.MAX_VALUE
                        && expiry.longValue() <= System.currentTimeMillis()) return null;
                if (!(rawToken.get("loginId") instanceof String loginId)
                        || !loginId.equals(userId.toString())) return null;
                @SuppressWarnings("unchecked")
                Map<String, Object> tokens = (Map<String, Object>) rawTokens;
                tokens.remove(tokenValue);
                if (ext.get("tokenSessions") instanceof Map<?, ?> rawTokenSessions) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tokenSessions = (Map<String, Object>) rawTokenSessions;
                    tokenSessions.remove(tokenValue);
                }
                if (!removeTokenFromAccountSession(ext, userId, tokenValue)) return null;
                UserDO update = new UserDO();
                update.setExtInfo(JSONUtils.toJsonString(ext));
                update.setUpdateTime(LocalDateTime.now());
                QueryWrapper query = QueryWrapper.create().where(UserDO::getId).eq(userId);
                query.and(UserDO::getExtInfo).eq(expectedExtInfo);
                if (userMapper.updateByQuery(update, query) == 1) return userId;
            }
        }
        return null;
    }

    private boolean removeTokenFromAccountSession(
            Map<String, Object> ext, Long userId, String tokenValue) {
        if (!(ext.get("sessions") instanceof Map<?, ?> rawSessions)) return true;
        Object rawEntry = rawSessions.get(userId.toString());
        if (!(rawEntry instanceof Map<?, ?> sessionEntry)
                || !(sessionEntry.get("data") instanceof String sessionData)) return true;
        SaSession session;
        try {
            session = JSONUtils.parseObject(sessionData, SaSession.class);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (session == null) return false;
        if (session.getTerminalList() == null) return true;
        List<SaTerminalInfo> terminals = session.terminalListCopy();
        boolean removed = terminals.removeIf(
                terminal -> Objects.equals(tokenValue, terminal.getTokenValue()));
        if (!removed) return true;
        session.setTerminalList(terminals);
        if (terminals.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sessions = (Map<String, Object>) rawSessions;
            sessions.remove(userId.toString());
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> mutableEntry = (Map<String, Object>) sessionEntry;
            mutableEntry.put("data", JSONUtils.toJsonString(session));
        }
        return true;
    }
}
