package com.shiyu.ai.platform.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动平台应用并装配跨模块基础设施。
 */
@SpringBootApplication(scanBasePackages = "com.shiyu.ai")
public class PlatformBootstrapApplication {

    /**
     * {@code main} 执行当前类型定义的业务操作。
     *
     * @param args 参数值，用于执行当前操作。
     */
    public static void main(String[] args) {
        SpringApplication.run(PlatformBootstrapApplication.class, args);
    }
}
