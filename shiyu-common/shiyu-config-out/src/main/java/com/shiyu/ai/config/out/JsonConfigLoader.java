package com.shiyu.ai.config.out;

import com.shiyu.ai.common.json.utils.JsonUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.Map;

public class JsonConfigLoader {

    private final ConfigurableEnvironment environment;

    public JsonConfigLoader(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * 加载 JSON 文件（绝对路径）到 Spring Environment
     */
    public void loadJsonFile(String absolutePath) {
        try {
            File file = new File(absolutePath);
            if (!file.exists()) {
                throw new RuntimeException("JSON file not found at: " + absolutePath);
            }

            Map<String, Object> map = JsonUtils.parseMap(file);
            MapPropertySource propertySource = new MapPropertySource("json-property", FlattenUtil.flatten(map));
            environment.getPropertySources().addFirst(propertySource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON config", e);
        }
    }
}
