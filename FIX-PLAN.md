# Shiyu-AI 后端修复计划（已根据反馈调整）

> 扫描时间：2026-07-07
> 约束条件：开发中阶段、不引入外部组件（Redis 等）、H2 无问题、Spring Boot 4.1.0 确认存在

---

## 第 1 轮 — 高优先级（预估：1 天）

### 1.1 API Key 占位符替换

**文件**: `shiyu-ai-core/src/main/resources/application-ai.yml`

```yaml
# 修改前
spring:
  ai:
    openai:
      api-key: xxxxxxxxxxxxxx

# 修改后 — 从环境变量加载
spring:
  ai:
    openai:
      api-key: ${AI_OPENAI_API_KEY:}
```

同时确认 `GenericPlatformAdapter.java` 中 `isApiKeyConfigured()` 兜底逻辑有效（当前已实现：如果 apiKey 为空或空白，返回 false 不创建 ChatModel）。

---

### 1.2 确认 password 字段不返回前端

**位置**: `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/vo/UserVO.java`

**检查要点**：`UserServiceImpl.getUserList()` 通过 `MapstructUtils.convert(result.getRight(), UserVO.class)` 做 DO→VO 转换。

```java
// 确认 UserVO 中没有 password 字段
@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickName;
    // private String password;  ← 确认这行不存在
    ...
}
```

如果 UserVO 无 password 字段则无需操作；如有则删掉或加 `@JsonIgnore`。

---

<!-- AI 平台权限：当前阶段全局登录校验足够，生产上线前再补充 @SaCheckRole -->

---

## 第 2 轮 — Token 存储缺陷修复（预估：1-2 天）

### 2.1 避免 Token 明文暴露 userId

**文件**: `SaTokenConfig.java` + `SaTokenDaoImpl.java`

**现状**: Token 格式为 `{userId}_{random60}` → 从 token 可反解用户 ID。

**修复**（在已有框架内，不引入 Redis）：

```java
// SaTokenConfig.java - rewriteSaStrategy
@PostConstruct
public void rewriteSaStrategy() {
    // 改为不包含 userId 的纯随机 token
    SaStrategy.instance.createToken = (loginId, loginType) ->
            SaFoxUtil.getRandomString(64);  // 随机 64 位字符串
}
```

对应修改 `SaTokenDaoImpl.parseUserIdFromToken()` — 不能再从 token 解析，需从 value 映射反向查找。

**方案**：`SaTokenDaoImpl` 增加 token → userId 的 Caffeine 反向索引：

```java
// 新增反向索引
private final Cache<String, Long> tokenToUserCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)   // 比 token 过期长即可
        .maximumSize(50000)
        .build();

// 修改 set() - 存入时建立索引
@Override
public void set(String key, String value, long timeout) {
    // value 就是 loginId (userId)
    // 记录反向映射
    tokenToUserCache.put(key, Long.parseLong(value));
    // ... 其余逻辑不变
}

// 修改 extractUserIdFromTokenKey() - 优先查缓存
private Long extractUserIdFromTokenKey(String key) {
    // 先从反向索引查
    Long userId = tokenToUserCache.getIfPresent(key);
    if (userId != null) return userId;
    // 旧逻辑兜底（兼容已存在的旧格式 token）
    String tokenValue = key.substring(TOKEN_PREFIX.length());
    return parseUserIdFromToken(tokenValue);
}

// parseUserIdFromToken 保留下划线格式的旧解析，但新 token 无下划线则去 DB 查 extInfo
```

---

### 2.2 避免 Java 原生反序列化

**文件**: `SaTokenDaoImpl.java` 第 478-498 行

```java
// 修改前 — Java 原生序列化（反序列化漏洞风险）
private String serializeSession(SaSession session) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(session);
    return Base64.getEncoder().encodeToString(bos.toByteArray());
}

private SaSession deserializeSession(String data) {
    byte[] bytes = Base64.getDecoder().decode(data);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
    return (SaSession) ois.readObject();
}

// 修改后 — 使用 JSON 序列化
private String serializeSession(SaSession session) {
    return JSONUtils.toJsonString(session);
}

private SaSession deserializeSession(String data) {
    return JSONUtils.parseObject(data, SaSession.class);
}
```

> 注意：如果 session 中存了 SaSession 的 SaRole/SaPermission 等非 JSON 友好对象，需要确保有正确的 Jackson 配置或自定义序列化器。

---

### 2.3 Caffeine localCache 过期时间与 Token 真实过期对齐

**文件**: `SaTokenDaoImpl.java` 第 27-30 行

```java
// 修改前 — 固定 3 秒，与 token 真实过期不同步
private final Cache<String, Object> localCache = Caffeine.newBuilder()
        .expireAfterWrite(3, TimeUnit.SECONDS)
        .maximumSize(10000)
        .build();

// 修改后 — 根据 token 过期时间动态设置
// 改为使用更长的缓存，get() 中已有 isExpired() 判断兜底
private final Cache<String, Object> localCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)   // 30 秒，减少 DB 访问
        .maximumSize(50000)                        // 支持更多并发用户
        .build();
```

---

## 第 3 轮 — 限流与验证码加固（预估：1 天）

### 3.1 登录限流支持多实例

**文件**: `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/handler/LoginRateLimiter.java`

当前为单机内存实现，多实例部署下限流失效。在不引入 Redis 的前提下：

**方案 A（当前方案增强）**：使用 `random` 时间窗口抖动，增加暴力破解成本

```java
// 改为滑动窗口 + 随机抖动
public boolean isAllowed(String ip) {
    long now = System.currentTimeMillis();
    int[] timestamps = attempts.computeIfAbsent(ip, k -> new int[]{0, 0, 0});
    // timestamps[0] = windowStart, timestamps[1] = count, timestamps[2] = lockedUntil
    synchronized (timestamps) {
        // 检查是否被封禁
        if (timestamps[2] > now) {
            log.warn("IP 已被临时封禁: {}", ip);
            return false;
        }
        // 超过窗口则重置
        if (timestamps[0] < now - windowMs) {
            timestamps[0] = (int) now;
            timestamps[1] = 1;
            return true;
        }
        timestamps[1]++;
        return timestamps[1] <= maxAttempts;
    }
}
```

**方案 B（推荐）**：确认是否接受基于 IP 的本地限流。多实例场景下每个实例各自计数，攻击者请求分散到不同实例即可突破。如果必须多实例协同，建议评估引入 Redis 的需要。

---

### 3.2 验证码增强

**文件**: `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/service/impl/CaptchaServiceImpl.java`

```java
// 1. 增加验证码长度
private static final int CAPTCHA_LENGTH = 6;     // 4 → 6

// 2. 增加尝试次数限制（同一 key 最多 3 次）
private final Map<String, Integer> attemptCount = new ConcurrentHashMap<>();
private static final int MAX_ATTEMPTS = 3;

@Override
public boolean validateCaptcha(String key, String code) {
    // 先检查尝试次数
    Integer attempts = attemptCount.getOrDefault(key, 0);
    if (attempts >= MAX_ATTEMPTS) {
        destroyCaptcha(key);
        log.warn("验证码尝试超限：key={}", key);
        return false;
    }
    attemptCount.put(key, attempts + 1);
    // ... 原有验证逻辑
}

// 3. Key 使用 UUID 而非时间戳 + 10000 随机
private String generateCaptchaKey() {
    return "captcha_" + UUID.randomUUID().toString();
}
```

### 3.3 验证码 Map 定期清理

```java
@PostConstruct
public void init() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(e -> e.getValue().isExpired());
        attemptCount.entrySet().removeIf(e -> {
            CaptchaData data = captchaStore.get(e.getKey());
            return data == null || data.isExpired();
        });
    }, 5, 5, TimeUnit.MINUTES);
}
```

---

## 第 4 轮 — 优化项（预估：1 天）

### 4.1 日志脱敏

**文件**: `CaptchaServiceImpl.java` 第 96 行

```java
// 修改前
log.info("生成验证码：key={}, code={}, expireTime={}", key, code, expireTime);

// 修改后
log.info("生成验证码：key={}, expireTime={}s", key, CAPTCHA_EXPIRE_TIME / 1000);
```

### 4.2 MySQL artifact 名修正

**文件**: `pom.xml`

```xml
<!-- 修改前 -->
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>

<!-- 修改后 -->
<groupId>com.mysql</groupId>
<artifactId>mysql-connector-j</artifactId>
```

> 注意：`mysql-connector-java` 从 8.0.31+ 起已重命名为 `mysql-connector-j`，groupId 也从 `mysql` 改为 `com.mysql`。当前版本 `9.4.0` 需要新坐标。

### 4.3 XSS 验证器升级

**文件**: `shiyu-common/shiyu-common-core/src/main/java/com/shiyu/ai/common/core/xss/XssValidator.java`

```java
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssValidator implements ConstraintValidator<Xss, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true;
        String cleaned = Jsoup.clean(value, Safelist.basic());
        return cleaned.equals(value);
    }
}
```

pom.xml 添加：

```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.3</version>
</dependency>
```

### 4.4 默认密码不共享

**文件**: `PasswordUtils.java`

```java
// 删除类级别常量（所有用户共用同一个默认密码）
// public static final String DEFAULT_PASSWORD = generateRandomPassword();

// 改为方法调用
public static String generateDefaultPassword() {
    return generateRandomPassword();
}

// 调用处（UserServiceImpl.createUser）：
// userBO.setPassword(PasswordUtils.encode(PasswordUtils.DEFAULT_PASSWORD));
// → 改为
userBO.setPassword(PasswordUtils.encode(PasswordUtils.generateDefaultPassword()));
```

---

## 最终实施路径

```
第 1 轮 [0.5天] — 阻断直接漏洞
  ├── API Key 环境变量化
  └── 确认 password 不返前端

第 2 轮 [1-2天] — Token 存储加固（不引入外部组件）
  ├── Token 纯随机化（不暴露 userId）
  ├── 反序列化 JSON 化
  └── Caffeine 缓存参数调整

第 3 轮 [1天]   — 限流 + 验证码
  ├── 限流滑动窗口增强
  ├── 验证码 4→6 位 + 尝试限制
  └── Map 定期清理

第 4 轮 [1天]   — 杂项优化
  ├── 日志脱敏
  ├── MySQL artifact 修正
  ├── XSS 验证器升级
  └── 默认密码不再共享
```

**总计**: 3.5-4.5 人天，不引入外部组件，开发环境无影响。
