package com.shiyu.ai.bootstrap;

import com.shiyu.ai.bootstrap.lock.EmbeddedDataDirectoryLock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动 Shiyu Bootstrap 应用并装配项目所需的运行基础设施。
 */
@SpringBootApplication(
        scanBasePackages = {"com.shiyu.ai.bootstrap", "com.shiyu.ai.composition"})
public class ShiyuBootstrapApplication {

    /**
     * 数据目录锁，表示当前对象中的对应属性。
     */
    private static EmbeddedDataDirectoryLock dataDirectoryLock;

    /**
     * 执行 Shiyu Bootstrap 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param args 用于完成本次业务处理的 args 参数。
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
