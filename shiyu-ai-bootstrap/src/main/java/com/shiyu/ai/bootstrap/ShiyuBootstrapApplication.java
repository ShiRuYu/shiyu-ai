package com.shiyu.ai.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shiyu.ai")
public class ShiyuBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiyuBootstrapApplication.class, args);
    }

}
