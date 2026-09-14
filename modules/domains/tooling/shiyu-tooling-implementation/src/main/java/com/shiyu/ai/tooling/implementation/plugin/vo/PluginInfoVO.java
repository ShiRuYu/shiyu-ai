package com.shiyu.ai.tooling.implementation.plugin.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code PluginInfoVO} 承载工具模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
public class PluginInfoVO implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private String id;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 版本，表示当前对象中的对应属性。
     */
    private String version;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String state;
    /**
     * loadedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String loadedAt;
}
