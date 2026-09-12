package com.shiyu.ai.iam.implementation.application.recovery;

import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 编排 AuthRecovery 应用用例。
 */
@Slf4j
public final class AuthRecoveryUseCase {
    /**
     * 用户仓储，表示当前对象中的对应属性。
     */
    private final UserRepository userRepository;
    /**
     * captchaService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CaptchaService captchaService;

    /**
     * {@code AuthRecoveryUseCase} 创建并初始化当前类型实例。
     *
     * @param userRepository 参数值，用于执行当前操作。
     * @param captchaService 参数值，用于执行当前操作。
     */
    public AuthRecoveryUseCase(UserRepository userRepository, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.captchaService = captchaService;
    }

    /**
     * {@code forgetPassword} 执行当前类型定义的业务操作。
     *
     * @param email 参数值，用于执行当前操作。
     * @param newPassword 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param captchaKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean forgetPassword(
            String email, String newPassword, String code, String captchaKey) {
        log.info("忘记密码: emailPresent={}", email != null);
        if (!captchaService.validateCaptcha(captchaKey, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        UserBO user = userRepository.selectByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("该邮箱未注册: " + email);
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        boolean updated = userRepository.update(user);
        if (updated) {
            SaTokenHelper.getInstance().logout(user.getId());
            SaTokenHelper.clearUserContextSession();
        }
        log.info("密码重置成功: userIdPresent={}", user.getId() != null);
        return updated;
    }
}
