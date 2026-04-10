package com.shiyu.ai.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 * 在应用启动完成后打印相关访问地址
 */
@Slf4j
@Component
public class ApplicationStartupListener implements CommandLineRunner {

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("应用启动成功！");
        log.info("Knife4j 文档地址: http://localhost:{}/doc.html", serverPort);
        log.info("Swagger UI 地址: http://localhost:{}/swagger-ui/index.html", serverPort);
        log.info("H2 控制台地址: http://localhost:{}/h2", serverPort);
        log.info("========================================");
    }
}
