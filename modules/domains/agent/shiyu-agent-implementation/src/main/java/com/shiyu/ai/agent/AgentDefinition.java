package com.shiyu.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示 智能体 相关流程中的状态、关系或执行数据。
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

    /**
     * 创建或保存 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     */
    @Builder.Default private Map<String, AgentVersion> versions = new HashMap<>();


    public void addVersion(AgentVersion version) {
        if (versions == null) {
            versions = new HashMap<>();
        }
        versions.put(version.getVersionNumber(), version);
    }

    /**
     * 查询 智能体 相关业务数据，并返回处理结果。
     *
     * @param versionNumber 用于完成本次业务处理的 versionNumber 参数。
     * @return 返回 智能体 相关操作生成的结果数据。
     */
    public AgentVersion getVersion(String versionNumber) {
        if (versions == null) return null;
        if (versionNumber == null || versionNumber.isBlank()) {
            versionNumber = currentVersion;
        }
        return versionNumber != null ? versions.get(versionNumber) : null;
    }
}
