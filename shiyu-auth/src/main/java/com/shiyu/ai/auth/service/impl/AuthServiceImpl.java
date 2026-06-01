package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.LoginResponseVO;
import com.shiyu.ai.auth.domain.vo.LoginVO;
import com.shiyu.ai.auth.domain.vo.SysUserVO;
import com.shiyu.ai.auth.repository.SysUserRepository;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.utils.JwtTokenUtil;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository sysUserRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponseVO login(LoginVO loginVO) {
        try {
            // 1. 参数校验
            validateLoginParams(loginVO);

            // 2. 查询用户信息
            SysUserBO sysUserBO = sysUserRepository.getByUsername(loginVO.getUsername());
            if (sysUserBO == null) {
                log.warn("登录失败：用户不存在 - {}", loginVO.getUsername());
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 3. 用户状态校验
            validateUserStatus(sysUserBO);

            // 4. 使用 Spring Security 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginVO.getUsername(), loginVO.getPassword())
            );

            // 5. 设置认证信息到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 6. 生成访问令牌和刷新令牌
            String accessToken = jwtTokenUtil.generateAccessToken(sysUserBO);
            String refreshToken = jwtTokenUtil.generateRefreshToken(sysUserBO);

            // 7. 构建用户视图对象
            SysUserVO sysUserVO = convertToVO(sysUserBO);

            // 8. 返回登录响应
            return LoginResponseVO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(7200L) // 2 小时过期
                    .userInfo(sysUserVO)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("登录失败：用户名或密码错误 - {}", loginVO.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        } catch (IllegalArgumentException e) {
            log.warn("登录失败：{} - {}", e.getMessage(), loginVO.getUsername());
            throw e;
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

    @Override
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    @Override
    public String refreshToken(String oldToken) {
        if (!jwtTokenUtil.canTokenBeRefreshed(oldToken)) {
            log.warn("Token 无法刷新：{}", oldToken);
            return null;
        }
        
        String newToken = jwtTokenUtil.refreshToken(oldToken);
        log.info("Token 已刷新：{} -> {}", oldToken.substring(0, Math.min(20, oldToken.length())), 
                newToken != null ? newToken.substring(0, Math.min(20, newToken.length())) : "null");
        return newToken;
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
     * 校验登录参数
     *
     * @param loginVO 登录信息
     */
    private void validateLoginParams(LoginVO loginVO) {
        if (loginVO.getUsername() == null || loginVO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (loginVO.getPassword() == null || loginVO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        // TODO: 如果启用了验证码，可以在这里添加验证码校验逻辑
        // if (captchaEnabled && !validateCaptcha(loginVO)) {
        //     throw new IllegalArgumentException("验证码错误");
        // }
    }

    /**
     * 校验用户状态
     *
     * @param sysUserBO 用户业务对象
     */
    private void validateUserStatus(SysUserBO sysUserBO) {
        // 检查用户是否被删除
        if ("1".equals(sysUserBO.getDelFlag())) {
            log.warn("用户已被删除：{}", sysUserBO.getUserName());
            throw new BadCredentialsException("用户不存在或已被删除");
        }

        // 检查用户状态（1正常 0停用）
        if ("0".equals(sysUserBO.getStatus())) {
            log.warn("用户已被停用：{}", sysUserBO.getUserName());
            throw new IllegalArgumentException("用户账号已被停用，请联系管理员");
        }
    }
}
