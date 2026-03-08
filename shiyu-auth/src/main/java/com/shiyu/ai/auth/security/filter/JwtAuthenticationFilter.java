package com.shiyu.ai.auth.security.filter;

import com.shiyu.ai.auth.security.service.CustomUserDetailsService;
import com.shiyu.ai.auth.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求中获取 JWT token
            String jwt = parseJwt(request);
            
            if (StringUtils.hasText(jwt)) {
                // 验证 token
                if (JwtTokenUtil.validateToken(jwt)) {
                    // 从 token 中获取用户名
                    String username = JwtTokenUtil.getUsernameFromToken(jwt);
                                
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 加载用户详情
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                    
                        // 验证 token 中的用户与当前用户是否匹配
                        if (JwtTokenUtil.validateToken(jwt)) {
                            // 创建认证令牌
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );
                            
                            // 设置认证详情
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            
                            // 将认证信息设置到安全上下文
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            log.debug("JWT 认证成功：{}", username);
                        }
                    }
                } else {
                    log.warn("JWT Token 无效或已过期");
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证失败：{}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 解析 JWT Token
     *
     * @param request HTTP 请求
     * @return JWT Token
     */
    private String parseJwt(HttpServletRequest request) {
        // 1. 从 Authorization header 中获取
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        // 2. 从请求参数中获取（适用于 WebSocket 等场景）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
}
