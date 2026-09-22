package com.shiyu.ai.common.foundation.factory;

import cn.hutool.core.util.StrUtil;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * 创建或提供 Yml Property Source 相关的业务组件和运行时能力。
 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    /**
     * 创建或保存 Yml Property Source 相关业务数据，并返回处理结果。
     *
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param resource 用于完成本次业务处理的 resource 参数。
     * @return 返回 Yml Property Source 相关操作生成的结果数据。
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
