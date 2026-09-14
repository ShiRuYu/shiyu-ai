package com.shiyu.ai.bootstrap.lifecycle;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** 应用启动监听器 在应用启动完成后打印相关访问地址 */
@Slf4j
@Component
public class ApplicationStartupListener implements CommandLineRunner {

    /**
     * 服务端口，表示当前对象中的对应属性。
     */
    @Value("${server.port:8080}")
    private Integer serverPort;

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param args 参数值，用于执行当前操作。
     */
    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("应用启动成功！");
        log.info("OpenAPI JSON 地址: http://localhost:{}/v3/api-docs", serverPort);
        log.info("API 文档 UI 仅在启用 api-docs-ui profile 时提供");
        log.info("H2 控制台地址: http://localhost:{}/h2", serverPort);
        log.info("========================================");
    }
}
