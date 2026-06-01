package com.shiyu.ai.auth.security.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.SysRoleDO;
import com.shiyu.ai.auth.domain.SysUserRoleDO;
import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.mapper.SysRoleMapper;
import com.shiyu.ai.auth.mapper.SysUserRoleMapper;
import com.shiyu.ai.auth.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
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

            // 从数据库加载用户角色
            QueryWrapper rqw = new QueryWrapper();
            rqw.eq(SysUserRoleDO::getUserId, sysUserBO.getUserId());
            List<SysUserRoleDO> userRoles = sysUserRoleMapper.selectListByQuery(rqw);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (SysUserRoleDO ur : userRoles) {
                SysRoleDO role = sysRoleMapper.selectOneById(ur.getRoleId());
                if (role != null && "0".equals(role.getStatus())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleKey()));
                }
            }
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return User.builder()
                    .username(sysUserBO.getUserName())
                    .password(sysUserBO.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户信息失败：{}", username, e);
            throw new UsernameNotFoundException("加载用户信息失败：" + e.getMessage(), e);
        }
    }
}
