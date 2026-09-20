package com.shiyu.ai.bootstrap.lifecycle;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 处理 Application Startup 相关事件或请求，并推进后续业务流程。
 */
@Slf4j
@Component
public class ApplicationStartupListener implements CommandLineRunner {

    /**
     * 服务端口，表示当前对象中的对应属性。
     */
    @Value("${server.port:8080}")
    private Integer serverPort;

    /**
     * 执行 Application Startup 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param args 用于完成本次业务处理的 args 参数。
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
