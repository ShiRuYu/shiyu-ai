package com.shiyu.ai.config.out;

import com.shiyu.ai.common.core.utils.FlattenUtil;
import com.shiyu.ai.common.core.utils.JsonUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.Optional;

public class JsonConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        // 可以通过系统属性或环境变量指定 JSON 文件路径
        Optional.ofNullable(environment.getProperty("shiyu.config.path.json"))
                .ifPresent(jsonPath -> {
                    Map<String, Object> configMap = JsonUtils.loadJsonFile(jsonPath);
                    MapPropertySource propertySource = new MapPropertySource("out-json-property", FlattenUtil.flatten(configMap));
                    environment.getPropertySources().addFirst(propertySource);
                });

    }
}
