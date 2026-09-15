package com.shiyu.ai.common.core.module;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 保存业务模块配置元数据，供组合根和诊断端点读取。
 */
@ConfigurationProperties(prefix = "shiyu")
public class BusinessModuleProperties {

    private Map<String, ModuleState> modules = new LinkedHashMap<>();

    /**
     * 返回模块开关配置，调用方可据此生成诊断信息。
     *
     * @return 模块配置映射。
     */
    public Map<String, ModuleState> getModules() {
        return modules;
    }

    /**
     * 设置模块开关配置。
     *
     * @param modules 模块配置映射。
     */
    public void setModules(Map<String, ModuleState> modules) {
        this.modules = modules == null ? new LinkedHashMap<>() : new LinkedHashMap<>(modules);
    }

    /**
     * 描述单个模块的进程级开关。
     *
     * @param enabled 是否启用模块。
     */
    public record ModuleState(Boolean enabled) {}
}
