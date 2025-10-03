package com.shiyu.ai.config.out;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Optional;

public class JsonConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        // 可以通过系统属性或环境变量指定 JSON 文件路径
        Optional.ofNullable(environment.getProperty("shiyu.config.path.json"))
                .ifPresent(jsonPath -> {
                    JsonConfigLoader loader = new JsonConfigLoader(environment);
                    loader.loadJsonFile(jsonPath);
                });

    }
}
