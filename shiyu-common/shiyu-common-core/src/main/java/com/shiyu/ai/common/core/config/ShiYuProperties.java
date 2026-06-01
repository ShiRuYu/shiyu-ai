package com.shiyu.ai.common.core.config;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 */

@Data
@Component
@ConfigurationProperties(prefix = "shiyu")
@PropertySource(value = "classpath:shiyu-common.yml", factory = YmlPropertySourceFactory.class)
public class ShiYuProperties {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 实例演示开关
     */
    private boolean demoEnabled;

    /**
     * 获取地址开关
     */
    private boolean addressEnabled;

}
