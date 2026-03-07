package com.shiyu.ai.auth.utils;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 */
@Slf4j
public class JwtTokenUtil {

    /**
     * JWT 密钥（生产环境应该使用更复杂的密钥并存储在配置中心）
     */
    private static final String SECRET_KEY = "shiyu-ai-jwt-secret-key-2026-this-is-a-secure-key-for-demo";
    
    /**
     * 默认过期时间：2 小时（毫秒）
     */
    private static final long DEFAULT_EXPIRATION = 7200000L; // 2 hours
    
    /**
     * 刷新令牌有效期：7 天（毫秒）
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    /**
     * 从 Token 中获取用户名
     *
     * @param token 访问令牌
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从 Token 中获取用户名失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token 访问令牌
     * @return 用户 ID
     */
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return Long.valueOf(claims.get("userId", String.class));
        } catch (Exception e) {
            log.error("从 Token 中获取用户 ID 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取声明
     *
     * @param token 访问令牌
     * @return 声明
     */
    private static Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 生成访问令牌
     *
     * @param sysUserBO 用户信息
     * @return 访问令牌
     */
    public static String generateAccessToken(SysUserBO sysUserBO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUserBO.getUserId().toString());
        claims.put("username", sysUserBO.getUserName());
        
        return createToken(claims, sysUserBO.getUserName(), DEFAULT_EXPIRATION);
    }

    /**
     * 生成刷新令牌
     *
     * @param sysUserBO 用户信息
     * @return 刷新令牌
     */
    public static String generateRefreshToken(SysUserBO sysUserBO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUserBO.getUserId().toString());
        claims.put("type", "refresh");
        
        return createToken(claims, sysUserBO.getUserName(), REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 创建令牌
     *
     * @param claims 声明
     * @param subject 主题（用户名）
     * @param expiration 过期时间（毫秒）
     * @return 访问令牌
     */
    private static String createToken(Map<String, Object> claims, String subject, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token 访问令牌
     * @return true-有效，false-无效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token 已过期");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Token 验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否可以刷新
     *
     * @param token 访问令牌
     * @return true-可以刷新，false-不可以刷新
     */
    public static boolean canTokenBeRefreshed(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            // 只要没有过期就可以刷新
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新令牌
     *
     * @param oldToken 旧的访问令牌
     * @return 新的访问令牌
     */
    public static String refreshToken(String oldToken) {
        try {
            Claims claims = getClaimsFromToken(oldToken);
            Date now = new Date();
            Date newExpiration = new Date(now.getTime() + DEFAULT_EXPIRATION);
            
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(newExpiration)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("刷新令牌失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取 Token 的过期时间
     *
     * @param token 访问令牌
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取 Token 过期时间失败：{}", e.getMessage());
            return null;
        }
    }
}
