package com.shiyu.ai.iam.implementation.application.recovery;

import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;

import lombok.extern.slf4j.Slf4j;

/** Password recovery flow. Credential mutation happens only after captcha validation. */
@Slf4j
public final class AuthRecoveryUseCase {
    private final UserRepository userRepository;
    private final CaptchaService captchaService;

    public AuthRecoveryUseCase(UserRepository userRepository, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.captchaService = captchaService;
    }

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
