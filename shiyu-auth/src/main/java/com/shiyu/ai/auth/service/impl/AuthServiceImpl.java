package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.LoginResponseVO;
import com.shiyu.ai.auth.domain.vo.LoginVO;
import com.shiyu.ai.auth.domain.vo.SysUserVO;
import com.shiyu.ai.auth.repository.SysUserRepository;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Resource
    private SysUserRepository sysUserRepository;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseVO login(LoginVO loginVO) {
        try {
            // 1. 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginVO.getUsername(), loginVO.getPassword())
            );

            // 2. 设置认证信息到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. 转换为业务对象
            SysUserBO sysUserBO = sysUserRepository.getByUsername(loginVO.getUsername());
            if (sysUserBO == null) {
                throw new BadCredentialsException("用户不存在");
            }

            // 5. 生成访问令牌 (简单 UUID 示例，生产环境建议使用 JWT)
            String accessToken = generateToken(sysUserBO.getUserName());

            // 6. 构建用户视图对象
            SysUserVO sysUserVO = convertToVO(sysUserBO);

            // 7. 返回登录响应
            return LoginResponseVO.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(7200L) // 2 小时过期
                    .userInfo(sysUserVO)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("登录失败：用户名或密码错误 - {}", loginVO.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        } catch (Exception e) {
            log.error("登录失败：{}", loginVO.getUsername(), e);
            throw new RuntimeException("登录失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void logout() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        log.info("用户已登出");
    }

    @Override
    public SysUserBO getUserByUsername(String username) {
        return sysUserRepository.getByUsername(username);
    }

    /**
     * 将 BO 对象转换为 VO 对象
     *
     * @param sysUserBO 业务对象
     * @return 视图对象
     */
    private SysUserVO convertToVO(SysUserBO sysUserBO) {
        return MapstructUtils.convert(sysUserBO, SysUserVO.class);
    }

    /**
     * 生成访问令牌
     * 注意：这是一个简单的实现示例，生产环境应该使用 JWT 或其他安全的令牌生成机制
     *
     * @param username 用户名
     * @return 访问令牌
     */
    private String generateToken(String username) {
        // 简单实现：使用时间戳 + UUID 生成令牌
        // 生产环境建议使用 JJWT 或其他 JWT 库生成带有签名和过期时间的令牌
        return UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
    }
}
