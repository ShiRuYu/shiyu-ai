package com.shiyu.ai.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shiyu.ai")
public class ShiyuBootstrapApplication {

    private static EmbeddedDataDirectoryLock dataDirectoryLock;

    public static void main(String[] args) {
        dataDirectoryLock = EmbeddedDataDirectoryLock.acquire();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> dataDirectoryLock.close(), "embedded-data-lock-release"));
        try {
            SpringApplication.run(ShiyuBootstrapApplication.class, args);
        } catch (RuntimeException exception) {
            dataDirectoryLock.close();
            throw exception;
        }
    }

}
