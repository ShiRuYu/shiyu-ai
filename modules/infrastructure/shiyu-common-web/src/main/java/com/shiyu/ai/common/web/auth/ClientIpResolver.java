package com.shiyu.ai.common.web.auth;

/**
 * 根据请求上下文解析或路由 Client Ip 相关的处理能力。
 */
@FunctionalInterface
public interface ClientIpResolver {

    /**
     * 执行 Client Ip 相关业务数据，并返回处理结果。
     *
     * @return 返回 Client Ip 相关操作生成的结果数据。
     */
    String currentClientIp();
}
