package com.shiyu.ai.common.web.auth;

/**
 * ClientIpResolver 接口，定义基础设施模块的能力边界。
 */
@FunctionalInterface
public interface ClientIpResolver {

    /**
     * 执行 {@code currentClientIp} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String currentClientIp();
}
