package com.shiyu.ai.iam.implementation.application.recovery;

import com.shiyu.ai.common.foundation.utils.PasswordUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 定义 认证 Recovery 相关用例的输入、授权和业务结果。
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
     * 执行 认证 Recovery 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userRepository 用于完成本次业务处理的 userRepository 参数。
     * @param captchaService 用于完成本次业务处理的 captchaService 参数。
     */
    public AuthRecoveryUseCase(UserRepository userRepository, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.captchaService = captchaService;
    }

    /**
     * 执行 认证 Recovery 相关业务数据，并返回处理结果。
     *
     * @param email 用于完成本次业务处理的 email 参数。
     * @param newPassword 用于完成本次业务处理的 newPassword 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回本次条件判断是否成立。
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
