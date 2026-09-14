package com.shiyu.ai.common.core.factory;

import cn.hutool.core.util.StrUtil;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/** yml 配置源工厂 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    /**
     * {@code createPropertySource} 写入或更新当前模块中的业务数据。
     *
     * @param name 参数值，用于执行当前操作。
     * @param resource 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource)
            throws IOException {
        String sourceName = resource.getResource().getFilename();
        if (StrUtil.isNotBlank(sourceName)
                && (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml"))) {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return new PropertiesPropertySource(sourceName, factory.getObject());
        }
        return super.createPropertySource(name, resource);
    }
}
