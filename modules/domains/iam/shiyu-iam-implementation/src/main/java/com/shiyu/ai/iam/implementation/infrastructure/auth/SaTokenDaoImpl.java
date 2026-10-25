package com.shiyu.ai.iam.implementation.infrastructure.auth;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.SaTokenUserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 提供 Sa Token Dao Impl 所属基础设施的适配、存储或运行支持。
 */
@Component
public class SaTokenDaoImpl implements SaTokenDao, AtomicTokenConsumer {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SaTokenDaoImpl.class);

    /**
     * KEY_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String KEY_PREFIX = "Authorization:login:";
    /**
     * TOKEN_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String TOKEN_PREFIX = KEY_PREFIX + "token:";
    /**
     * SESSION_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String SESSION_PREFIX = KEY_PREFIX + "session:";
    /**
     * TOKEN_SESSION_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String TOKEN_SESSION_PREFIX = KEY_PREFIX + "token-session:";

    /**
     * saTokenUserRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final SaTokenUserRepository saTokenUserRepository;

    /** 主缓存用于对象存储；持久化 token 与 session 每次从数据库读取。 */
    private final Cache<String, Object> localCache =
            Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(50000).build();


    /** 定时清理过期 token 数据 */
    private final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * 执行 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param saTokenUserRepository 用于完成本次业务处理的 saTokenUserRepository 参数。
     */
    public SaTokenDaoImpl(SaTokenUserRepository saTokenUserRepository) {
        this.saTokenUserRepository = saTokenUserRepository;
    }

    @Override
    public Long consumeToken(String tokenValue) {
        Long userId = saTokenUserRepository.consumeToken(tokenValue);
        if (userId != null) {
            localCache.invalidate(TOKEN_PREFIX + tokenValue);
            localCache.invalidate(TOKEN_SESSION_PREFIX + tokenValue);
        }
        return userId;
    }

    /**
     * 执行 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     */
    @PostConstruct
    public void init() {
        // 每 30 分钟清理一次过期 token，防止 extInfo 无限膨胀
        cleanupScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        localCache.cleanUp();
                    } catch (Exception ignored) {
                        // 清理失败不影响主流程
                    }
                },
                30,
                30,
                TimeUnit.MINUTES);
    }

    // ==================== String 存储 (Token→loginId) ====================

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public String get(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return null;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return null;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return null;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return null;

        if (isExpired(entry)) {
            updateExtInfo(userId, latest -> {
                Map<String, Object> latestTokens = castMap(latest.get("tokens"));
                return latestTokens != null && latestTokens.remove(tokenValue) != null;
            });
            localCache.invalidate(key);
            return null;
        }

        return (String) entry.get("loginId");
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param value 用于完成本次业务处理的 value 参数。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        // value 就是 loginId（userId 的字符串形式），直接使用
        Long userId = Long.parseLong(value);

        long expireTime =
                timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE;
        String tokenValue = key.substring(TOKEN_PREFIX.length());
        updateExtInfo(userId, ext -> {
            Map<String, Object> tokens = getOrCreateMap(ext, "tokens");
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("loginId", value);
            entry.put("expireTime", expireTime);
            tokens.put(tokenValue, entry);
            return true;
        });
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param value 用于完成本次业务处理的 value 参数。
     */
    @Override
    public void update(String key, String value) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) {
            // 纯随机 token 缓存未命中时，尝试从 value 参数获取
            try {
                userId = Long.parseLong(value);
            } catch (NumberFormatException e) {
                return;
            }
        }

        Long resolvedUserId = userId;
        String tokenValue = key.substring(TOKEN_PREFIX.length());
        updateExtInfo(resolvedUserId, ext -> {
            Map<String, Object> tokens = castMap(ext.get("tokens"));
            Map<String, Object> entry = tokens == null ? null : castMap(tokens.get(tokenValue));
            if (entry == null) return false;
            entry.put("loginId", value);
            return true;
        });
    }

    /**
     * 删除或移除 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     */
    @Override
    public void delete(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return;

        String tokenValue = key.substring(TOKEN_PREFIX.length());
        updateExtInfo(userId, ext -> {
            Map<String, Object> tokens = castMap(ext.get("tokens"));
            Map<String, Object> tokenSessions = castMap(ext.get("tokenSessions"));
            boolean removed = tokens != null && tokens.remove(tokenValue) != null;
            removed |= tokenSessions != null && tokenSessions.remove(tokenValue) != null;
            return removed;
        });
        localCache.invalidate(TOKEN_SESSION_PREFIX + tokenValue);
    }

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public long getTimeout(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return NOT_VALUE_EXPIRE;

        Long userId = findUserIdByToken(key.substring(TOKEN_PREFIX.length()));
        if (userId == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return NOT_VALUE_EXPIRE;

        return getRemainingTimeout(entry);
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return;

        String tokenValue = key.substring(TOKEN_PREFIX.length());
        long expireTime = timeout > 0
                ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE;
        updateExtInfo(userId, ext -> {
            Map<String, Object> tokens = castMap(ext.get("tokens"));
            Map<String, Object> entry = tokens == null ? null : castMap(tokens.get(tokenValue));
            if (entry == null) return false;
            entry.put("expireTime", expireTime);
            return true;
        });
        localCache.invalidate(key);
    }

    // ==================== Object 存储（仅内存 Caffeine） ====================

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public Object getObject(String key) {
        return localCache.getIfPresent(key);
    }

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param classType 用于完成本次业务处理的 classType 参数。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object val = localCache.getIfPresent(key);
        if (val != null && classType.isInstance(val)) {
            return classType.cast(val);
        }
        return null;
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param object 用于完成本次业务处理的 object 参数。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        localCache.put(key, object);
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param object 用于完成本次业务处理的 object 参数。
     */
    @Override
    public void updateObject(String key, Object object) {
        localCache.put(key, object);
    }

    /**
     * 删除或移除 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     */
    @Override
    public void deleteObject(String key) {
        localCache.invalidate(key);
    }

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public long getObjectTimeout(String key) {
        return NOT_VALUE_EXPIRE;
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {}

    // ==================== Session 存储 ====================

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public SaSession getSession(String sessionId) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return null;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return null;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = castMap(ext.get(section));
        if (sectionMap == null) return null;

        Map<String, Object> entry = castMap(sectionMap.get(valueKey));
        if (entry == null) return null;

        if (isExpired(entry)) {
            updateExtInfo(userId, latest -> {
                Map<String, Object> latestSection = castMap(latest.get(section));
                return latestSection != null && latestSection.remove(valueKey) != null;
            });
            localCache.invalidate(sessionId);
            return null;
        }

        String data = (String) entry.get("data");
        if (data == null) return null;

        SaSession session = deserializeSession(data);
        return session;
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void setSession(SaSession session, long timeout) {
        String sessionId = session.getId();
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        long expireTime =
                timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE;
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;
        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";
        updateExtInfo(userId, ext -> {
            Map<String, Object> sectionMap = getOrCreateMap(ext, section);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", serializeSession(session));
            entry.put("expireTime", expireTime);
            sectionMap.put(valueKey, entry);
            return true;
        });
        localCache.put(sessionId, session);
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
     */
    @Override
    public void updateSession(SaSession session) {
        String sessionId = session.getId();
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;
        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";
        long[] expiry = {Long.MIN_VALUE};
        boolean updated = updateExtInfo(userId, ext -> {
            Map<String, Object> sectionMap = castMap(ext.get(section));
            Map<String, Object> entry = sectionMap == null ? null : castMap(sectionMap.get(valueKey));
            if (entry == null) return false;
            entry.put("data", serializeSession(session));
            if (entry.get("expireTime") instanceof Number timeout) expiry[0] = timeout.longValue();
            return true;
        });
        if (updated && expiry[0] != Long.MIN_VALUE) {
            localCache.put(sessionId, session);
        } else {
            localCache.invalidate(sessionId);
        }
    }

    /**
     * 删除或移除 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     */
    @Override
    public void deleteSession(String sessionId) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;
        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";
        updateExtInfo(userId, ext -> {
            Map<String, Object> sectionMap = castMap(ext.get(section));
            return sectionMap != null && sectionMap.remove(valueKey) != null;
        });
        localCache.invalidate(sessionId);
    }

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回 Sa Token Dao Impl 相关操作生成的结果数据。
     */
    @Override
    public long getSessionTimeout(String sessionId) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return NOT_VALUE_EXPIRE;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = castMap(ext.get(section));
        if (sectionMap == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> entry = castMap(sectionMap.get(valueKey));
        if (entry == null) return NOT_VALUE_EXPIRE;

        return getRemainingTimeout(entry);
    }

    /**
     * 更新或设置 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     */
    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;
        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";
        long expireTime = timeout > 0
                ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE;
        updateExtInfo(userId, ext -> {
            Map<String, Object> sectionMap = castMap(ext.get(section));
            Map<String, Object> entry = sectionMap == null ? null : castMap(sectionMap.get(valueKey));
            if (entry == null) return false;
            entry.put("expireTime", expireTime);
            return true;
        });
        localCache.invalidate(sessionId);
    }

    // ==================== 搜索 ====================

    /**
     * 查询 Sa Token Dao Impl 相关业务数据，并返回处理结果。
     *
     * @param prefix 用于完成本次业务处理的 prefix 参数。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param start 用于完成本次业务处理的 start 参数。
     * @param size 每页返回的数据数量。
     * @param sortType 用于完成本次业务处理的 sortType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<String> searchData(
            String prefix, String keyword, int start, int size, boolean sortType) {
        return new ArrayList<>();
    }

    // ==================== 内部辅助方法 ====================

    private Long extractUserIdFromTokenKey(String key) {
        String tokenValue = key.substring(TOKEN_PREFIX.length());
        return findUserIdByToken(tokenValue);
    }

    /** 从 token 字符串中解析 userId（缓存未命中时调用）。 */
    private Long findUserIdByToken(String tokenValue) {
        try {
            Long encodedUserId = parseUserIdFromToken(tokenValue);
            if (encodedUserId != null) return encodedUserId;
            return saTokenUserRepository.findUserIdByToken(tokenValue);
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long extractUserIdFromSessionKey(String sessionId) {
        try {
            String valueKey = extractSessionValueKey(sessionId);
            if (valueKey == null) return null;

            // session: 前缀时 valueKey 就是 loginId（纯数字），直接解析
            if (sessionId.startsWith(SESSION_PREFIX)) {
                return Long.parseLong(valueKey);
            }

            // token-session: 前缀时 valueKey 是 token 值，所有权必须来自当前持久化令牌
            if (sessionId.startsWith(TOKEN_SESSION_PREFIX)) {
                return findUserIdByToken(valueKey);
            }

            return Long.parseLong(valueKey);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractSessionValueKey(String sessionId) {
        if (sessionId.startsWith(TOKEN_SESSION_PREFIX)) {
            return sessionId.substring(TOKEN_SESSION_PREFIX.length());
        }
        if (sessionId.startsWith(SESSION_PREFIX)) {
            return sessionId.substring(SESSION_PREFIX.length());
        }
        return null;
    }

    /** 从 token 字符串中解析 userId。 token 格式：Base64(userId)_random50；纯数字仅用于 session: 前缀场景。 */
    private Long parseUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) return null;
        int underscore = token.indexOf('_');
        if (underscore < 0) {
            // 没有下划线 → 纯数字（session: 前缀场景）
            try {
                return Long.parseLong(token);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        String prefix = token.substring(0, underscore);
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(prefix);
            return Long.parseLong(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object obj) {
        if (obj instanceof Map) return (Map<String, Object>) obj;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
        return (Map<String, Object>) parent.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    private Map<String, Object> getExtInfo(Long userId) {
        UserBO user = saTokenUserRepository.selectById(userId);
        return parseExtInfo(user == null ? null : user.getExtInfo());
    }

    private Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo == null || extInfo.isBlank()) return new LinkedHashMap<>();
        Map<String, Object> map = JSONUtils.parseMap(extInfo);
        return map != null ? map : new LinkedHashMap<>();
    }

    private boolean updateExtInfo(Long userId, Function<Map<String, Object>, Boolean> mutation) {
        if (userId == null) return false;
        for (int attempt = 0; attempt < 8; attempt++) {
            UserBO user = saTokenUserRepository.selectById(userId);
            if (user == null) return false;
            String expectedExtInfo = user.getExtInfo();
            Map<String, Object> ext = parseExtInfo(expectedExtInfo);
            Map<String, Object> before = JSONUtils.parseMap(JSONUtils.toJsonString(ext));
            if (!Boolean.TRUE.equals(mutation.apply(ext))) return false;
            cleanupExpiredEntries(ext);
            if (Objects.equals(before, ext)) return true;
            user.setExtInfo(JSONUtils.toJsonString(ext));
            user.setUpdateTime(java.util.Date.from(java.time.Instant.now()));
            if (saTokenUserRepository.updateExtInfo(user, expectedExtInfo)) return true;
        }
        throw new IllegalStateException("Sa Token extInfo update conflicted repeatedly");
    }

    /** 清理 extInfo 中所有过期的 tokens / sessions / tokenSessions 条目 */
    private void cleanupExpiredEntries(Map<String, Object> ext) {
        for (String section : new String[] {"tokens", "sessions", "tokenSessions"}) {
            Map<String, Object> sectionMap = castMap(ext.get(section));
            if (sectionMap != null) {
                sectionMap
                        .entrySet()
                        .removeIf(
                                entry -> {
                                    Map<String, Object> entryMap = castMap(entry.getValue());
                                    return entryMap != null && isExpired(entryMap);
                                });
            }
        }
    }

    private boolean isExpired(Map<String, Object> entry) {
        Object expObj = entry.get("expireTime");
        if (expObj instanceof Number) {
            long expireTime = ((Number) expObj).longValue();
            return expireTime < System.currentTimeMillis() && expireTime != Long.MAX_VALUE;
        }
        return false;
    }

    private long getRemainingTimeout(Map<String, Object> entry) {
        Object expObj = entry.get("expireTime");
        if (expObj instanceof Number) {
            long expireTime = ((Number) expObj).longValue();
            if (expireTime == Long.MAX_VALUE) return NEVER_EXPIRE;
            long remain = expireTime - System.currentTimeMillis();
            return remain > 0 ? remain / 1000 : NOT_VALUE_EXPIRE;
        }
        return NOT_VALUE_EXPIRE;
    }

    /** 将 SaSession 序列化为 JSON 字符串 */
    private String serializeSession(SaSession session) {
        return JSONUtils.toJsonString(session);
    }

    /** 从 JSON 字符串反序列化为 SaSession */
    private SaSession deserializeSession(String data) {
        return JSONUtils.parseObject(data, SaSession.class);
    }

    /**
     * 执行 Sa Token Dao Impl 相关业务操作，并维护必要的状态和协作关系。
     */
    @PreDestroy
    public void destroy() {
        cleanupScheduler.shutdownNow();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("SaTokenDaoImpl 定时清理线程池未能正常关闭");
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("SaTokenDaoImpl 定时清理线程池已关闭");
    }
}
