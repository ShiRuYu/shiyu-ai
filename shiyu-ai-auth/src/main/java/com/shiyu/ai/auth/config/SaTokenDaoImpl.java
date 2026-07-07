package com.shiyu.ai.auth.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.dal.repository.auth.SaTokenUserRepository;
import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SaTokenDaoImpl implements SaTokenDao {

    private static final String KEY_PREFIX = "Authorization:login:";
    private static final String TOKEN_PREFIX = KEY_PREFIX + "token:";
    private static final String SESSION_PREFIX = KEY_PREFIX + "session:";
    private static final String TOKEN_SESSION_PREFIX = KEY_PREFIX + "token-session:";

    private final SaTokenUserRepository saTokenUserRepository;

    /**
     * 主缓存：存储 token → loginId 及 session 对象
     * 过期时间设为 30 秒，get() 中已有 isExpired() 兜底检查，减少 DB 访问
     */
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(50000)
            .build();

    /**
     * 反向索引缓存：token → userId
     * 由于 token 已改为纯随机字符串，不再包含 userId，需要用此缓存快速查找
     */
    private final Cache<String, Long> tokenToUserCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(50000)
            .build();

    /**
     * 定时清理过期 token 数据
     */
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public SaTokenDaoImpl(SaTokenUserRepository saTokenUserRepository) {
        this.saTokenUserRepository = saTokenUserRepository;
    }

    @PostConstruct
    public void init() {
        // 每 30 分钟清理一次过期 token，防止 extInfo 无限膨胀
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                localCache.cleanUp();
                tokenToUserCache.cleanUp();
            } catch (Exception ignored) {
                // 清理失败不影响主流程
            }
        }, 30, 30, TimeUnit.MINUTES);
    }

    // ==================== String 存储 (Token→loginId) ====================

    @Override
    public String get(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return null;

        Object cached = localCache.getIfPresent(key);
        if (cached instanceof String s) return s;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return null;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return null;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return null;

        if (isExpired(entry)) {
            tokens.remove(tokenValue);
            saveExtInfo(userId, ext);
            return null;
        }

        String loginId = (String) entry.get("loginId");
        if (loginId != null) {
            localCache.put(key, loginId);
        }
        return loginId;
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        // value 就是 loginId（userId 的字符串形式），直接使用
        Long userId = Long.parseLong(value);

        // 建立反向索引：token → userId（供后续 get/delete/session 查询使用）
        tokenToUserCache.put(key, userId);

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        // 仅保留最近登录的一个 token，清理该用户下所有旧 token
        Map<String, Object> tokens = getOrCreateMap(ext, "tokens");

        @SuppressWarnings("unchecked")
        List<String> oldTokens = new ArrayList<>(tokens.keySet());
        for (String oldToken : oldTokens) {
            localCache.invalidate(TOKEN_PREFIX + oldToken);
            localCache.invalidate(TOKEN_SESSION_PREFIX + oldToken);
        }
        tokens.clear();

        Map<String, Object> oldTokenSessions = castMap(ext.get("tokenSessions"));
        if (oldTokenSessions != null) {
            oldTokenSessions.clear();
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("loginId", value);
        entry.put("expireTime", timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE);
        tokens.put(tokenValue, entry);

        saveExtInfo(userId, ext);
        localCache.put(key, value);
    }

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
            // 补充反向索引
            tokenToUserCache.put(key, userId);
        }

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return;

        entry.put("loginId", value);
        saveExtInfo(userId, ext);
        localCache.put(key, value);
    }

    @Override
    public void delete(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        Long userId = extractUserIdFromTokenKey(key);
        // 清理反向索引（先查再删不影响查）
        tokenToUserCache.invalidate(key);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens != null) {
            tokens.remove(tokenValue);
        }

        Map<String, Object> tokenSessions = castMap(ext.get("tokenSessions"));
        if (tokenSessions != null) {
            tokenSessions.remove(tokenValue);
        }

        saveExtInfo(userId, ext);
        localCache.invalidate(key);
        localCache.invalidate(TOKEN_SESSION_PREFIX + tokenValue);
    }

    @Override
    public long getTimeout(String key) {
        if (!key.startsWith(TOKEN_PREFIX)) return NOT_VALUE_EXPIRE;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return NOT_VALUE_EXPIRE;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return NOT_VALUE_EXPIRE;

        return getRemainingTimeout(entry);
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        if (!key.startsWith(TOKEN_PREFIX)) return;

        Long userId = extractUserIdFromTokenKey(key);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String tokenValue = key.substring(TOKEN_PREFIX.length());

        Map<String, Object> tokens = castMap(ext.get("tokens"));
        if (tokens == null) return;

        Map<String, Object> entry = castMap(tokens.get(tokenValue));
        if (entry == null) return;

        entry.put("expireTime", timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE);
        saveExtInfo(userId, ext);
    }

    // ==================== Object 存储（仅内存 Caffeine） ====================

    @Override
    public Object getObject(String key) {
        return localCache.getIfPresent(key);
    }

    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object val = localCache.getIfPresent(key);
        if (val != null && classType.isInstance(val)) {
            return classType.cast(val);
        }
        return null;
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        localCache.put(key, object);
    }

    @Override
    public void updateObject(String key, Object object) {
        localCache.put(key, object);
    }

    @Override
    public void deleteObject(String key) {
        localCache.invalidate(key);
    }

    @Override
    public long getObjectTimeout(String key) {
        return NOT_VALUE_EXPIRE;
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
    }

    // ==================== Session 存储 ====================

    @Override
    public SaSession getSession(String sessionId) {
       Object cached = localCache.getIfPresent(sessionId);
        if (cached instanceof SaSession ss) return ss;

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
            sectionMap.remove(valueKey);
            saveExtInfo(userId, ext);
            return null;
        }

        String data = (String) entry.get("data");
        if (data == null) return null;

        SaSession session = deserializeSession(data);
        if (session != null) {
            localCache.put(sessionId, session);
        }
        return session;
    }

    @Override
    public void setSession(SaSession session, long timeout) {
        String sessionId = session.getId();
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = getOrCreateMap(ext, section);

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("data", serializeSession(session));
        entry.put("expireTime", timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE);
        sectionMap.put(valueKey, entry);

        saveExtInfo(userId, ext);
        localCache.put(sessionId, session);
    }

    @Override
    public void updateSession(SaSession session) {
        String sessionId = session.getId();
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = castMap(ext.get(section));
        if (sectionMap == null) return;

        Map<String, Object> entry = castMap(sectionMap.get(valueKey));
        if (entry == null) return;

        entry.put("data", serializeSession(session));
        saveExtInfo(userId, ext);
        localCache.put(sessionId, session);
    }

    @Override
    public void deleteSession(String sessionId) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = castMap(ext.get(section));
        if (sectionMap != null) {
            sectionMap.remove(valueKey);
        }

        saveExtInfo(userId, ext);
        localCache.invalidate(sessionId);
    }

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

    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        Long userId = extractUserIdFromSessionKey(sessionId);
        if (userId == null) return;

        Map<String, Object> ext = getExtInfo(userId);
        String valueKey = extractSessionValueKey(sessionId);
        if (valueKey == null) return;

        String section = sessionId.startsWith(TOKEN_SESSION_PREFIX) ? "tokenSessions" : "sessions";

        Map<String, Object> sectionMap = castMap(ext.get(section));
        if (sectionMap == null) return;

        Map<String, Object> entry = castMap(sectionMap.get(valueKey));
        if (entry == null) return;

        entry.put("expireTime", timeout > 0 ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE);
        saveExtInfo(userId, ext);
    }

    // ==================== 搜索 ====================

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        return new ArrayList<>();
    }

    // ==================== 内部辅助方法 ====================

    private Long extractUserIdFromTokenKey(String key) {
        // 先从反向索引缓存查找
        Long cachedUserId = tokenToUserCache.getIfPresent(key);
        if (cachedUserId != null) return cachedUserId;

        // 缓存未命中，遍历所有用户的 extInfo 查找（兜底）
        String tokenValue = key.substring(TOKEN_PREFIX.length());
        return findUserIdByToken(tokenValue);
    }

    /**
     * 从 token 字符串中解析 userId（兜底方法，缓存未命中时调用）
     * 新格式 Base64(userId)_random50 和旧格式 userId_random60 均可解析
     */
    private Long findUserIdByToken(String tokenValue) {
        try {
            return parseUserIdFromToken(tokenValue);
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

            // token-session: 前缀时 valueKey 是 token 值，查反向缓存
            if (sessionId.startsWith(TOKEN_SESSION_PREFIX)) {
                String tokenKey = TOKEN_PREFIX + valueKey;
                Long cached = tokenToUserCache.getIfPresent(tokenKey);
                if (cached != null) return cached;

                // 缓存未命中，尝试旧格式 token 兼容（含下划线的旧 token）
                return parseUserIdFromToken(valueKey);
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

    /**
     * 从 token 字符串中解析 userId
     * 新格式：Base64(userId)_random50
     * 旧格式：userId_random60（兼容兜底）
     * 纯数字：session: 前缀场景，直接解析
     */
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
        // 先尝试 Base64 解码（新格式）
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(prefix);
            return Long.parseLong(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // 不是 Base64，尝试纯数字解析（旧格式兼容）
        }
        try {
            return Long.parseLong(prefix);
        } catch (NumberFormatException e) {
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
        UserDO user = saTokenUserRepository.selectById(userId);
        if (user == null || user.getExtInfo() == null || user.getExtInfo().isBlank()) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = JSONUtils.parseObject(user.getExtInfo(), Map.class);
        return map != null ? map : new LinkedHashMap<>();
    }

    private void saveExtInfo(Long userId, Map<String, Object> ext) {
        UserDO user = saTokenUserRepository.selectById(userId);
        if (user == null) return;
        user.setExtInfo(JSONUtils.toJsonString(ext));
        user.setUpdateTime(LocalDateTime.now());
        saTokenUserRepository.updateExtInfo(user);
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

    /**
     * 将 SaSession 序列化为 JSON 字符串
     */
    private String serializeSession(SaSession session) {
        return JSONUtils.toJsonString(session);
    }

    /**
     * 从 JSON 字符串反序列化为 SaSession
     */
    private SaSession deserializeSession(String data) {
        return JSONUtils.parseObject(data, SaSession.class);
    }
}
