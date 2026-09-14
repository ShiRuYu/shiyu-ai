package com.shiyu.ai.bootstrap;

import com.shiyu.ai.bootstrap.lock.EmbeddedDataDirectoryLock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类，负责创建 Spring Boot 运行上下文并释放启动期间的目录锁。
 */
@SpringBootApplication(scanBasePackages = "com.shiyu.ai")
public class ShiyuBootstrapApplication {

    /**
     * 数据目录锁，表示当前对象中的对应属性。
     */
    private static EmbeddedDataDirectoryLock dataDirectoryLock;

    /**
     * {@code main} 执行当前类型定义的业务操作。
     *
     * @param args 参数值，用于执行当前操作。
     */
    public static void main(String[] args) {
        dataDirectoryLock = EmbeddedDataDirectoryLock.acquire();
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> dataDirectoryLock.close(), "embedded-data-lock-release"));
        try {
            SpringApplication.run(ShiyuBootstrapApplication.class, args);
        } catch (RuntimeException exception) {
            dataDirectoryLock.close();
            throw exception;
        }
    }
}
