package com.shiyu.ai.auth.utils;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:shiyu-ai-jwt-secret-key-2026-this-is-a-secure-key-for-demo}")
    private String secretKey;

    @Value("${jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从 Token 中获取用户名失败：{}", e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) return null;
            return Long.valueOf(userIdObj.toString());
        } catch (Exception e) {
            log.error("从 Token 中获取用户 ID 失败：{}", e.getMessage());
            return null;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(SysUserBO sysUserBO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUserBO.getUserId().toString());
        claims.put("username", sysUserBO.getUserName());
        return createToken(claims, sysUserBO.getUserName(), accessTokenExpiration);
    }

    public String generateRefreshToken(SysUserBO sysUserBO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUserBO.getUserId().toString());
        claims.put("type", "refresh");
        return createToken(claims, sysUserBO.getUserName(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
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

    public boolean canTokenBeRefreshed(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String oldToken) {
        try {
            Claims claims = getClaimsFromToken(oldToken);
            Date now = new Date();
            Date newExpiration = new Date(now.getTime() + accessTokenExpiration);
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(newExpiration)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("刷新令牌失败：{}", e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取 Token 过期时间失败：{}", e.getMessage());
            return null;
        }
    }
}
