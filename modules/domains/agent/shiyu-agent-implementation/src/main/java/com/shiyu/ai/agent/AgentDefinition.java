package com.shiyu.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code AgentDefinition} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDefinition {
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * extInfo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, Object> extInfo;
    /**
     * currentVersion 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String currentVersion;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private long createdAt;
    /**
     * 更新时间，表示当前对象中的对应属性。
     */
    private long updatedAt;
    /**
     * startNodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String startNodeId;

    @Builder.Default private Map<String, AgentVersion> versions = new HashMap<>();

    /**
     * {@code addVersion} 执行当前类型定义的业务操作。
     *
     * @param version 参数值，用于执行当前操作。
     */
    public void addVersion(AgentVersion version) {
        if (versions == null) {
            versions = new HashMap<>();
        }
        versions.put(version.getVersionNumber(), version);
    }

    /**
     * {@code getVersion} 查询并返回当前操作所需的数据。
     *
     * @param versionNumber 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AgentVersion getVersion(String versionNumber) {
        if (versions == null) return null;
        if (versionNumber == null || versionNumber.isBlank()) {
            versionNumber = currentVersion;
        }
        return versionNumber != null ? versions.get(versionNumber) : null;
    }
}
