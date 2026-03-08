package com.shiyu.ai.auth.security.service;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository sysUserRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 根据用户名查询用户
            SysUserBO sysUserBO = sysUserRepository.getByUsername(username);
            
            if (sysUserBO == null) {
                log.warn("用户不存在：{}", username);
                throw new UsernameNotFoundException("用户不存在：" + username);
            }

            // 检查用户状态
            if ("0".equals(sysUserBO.getStatus())) {
                log.warn("用户已被停用：{}", username);
                throw new UsernameNotFoundException("用户已被停用：" + username);
            }

            // 构建 Spring Security 的 UserDetails
            // 注意：数据库中存储的应该是已经加密的密码，这里直接使用
            return User.builder()
                    .username(sysUserBO.getUserName())
                    .password(sysUserBO.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled("0".equals(sysUserBO.getStatus()))
                    .build();
                    
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户信息失败：{}", username, e);
            throw new UsernameNotFoundException("加载用户信息失败：" + e.getMessage(), e);
        }
    }
}
