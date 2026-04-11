package com.shiyu.ai.common.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类（BCrypt）
 * 提供密码加密和验证功能
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return true-密码正确，false-密码错误
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 判断密码是否需要重新加密（可选）
     *
     * @param encodedPassword 加密后的密码
     * @return true-需要重新加密，false-不需要
     */
    public static boolean upgradeEncoding(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return true;
        }
        return PASSWORD_ENCODER.upgradeEncoding(encodedPassword);
    }
}
