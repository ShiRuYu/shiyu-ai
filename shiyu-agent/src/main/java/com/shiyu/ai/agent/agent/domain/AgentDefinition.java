package com.shiyu.ai.agent.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 定义类
 * 包含 Agent 的基本信息和多个版本
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDefinition {
    
    /**
     * Agent ID（唯一标识）
     */
    private String agentId;
    
    /**
     * Agent 名称
     */
    private String name;
    
    /**
     * Agent 描述
     */
    private String description;
    
    /**
     * 版本号列表
     */
    @Builder.Default
    private List<AgentVersion> versions = new ArrayList<>();
    
    /**
     * 当前激活的版本号
     */
    private String currentVersion;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 获取指定版本
     * @param version 版本号
     * @return AgentVersion 实例，不存在则返回 null
     */
    public AgentVersion getVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            // 如果未指定版本，返回当前激活版本
            version = this.currentVersion;
        }
        
        if (version == null || version.trim().isEmpty()) {
            // 如果还是没有版本，返回第一个版本
            if (versions != null && !versions.isEmpty()) {
                version = versions.get(0).getVersionNumber();
            } else {
                log.warn("Agent 没有任何版本：{}", this.agentId);
                return null;
            }
        }
        
        String finalVersion = version;
        return versions.stream()
                .filter(v -> v.getVersionNumber().equals(finalVersion))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 添加版本
     * @param version 版本对象
     */
    public void addVersion(AgentVersion version) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        
        // 检查是否已存在该版本
        boolean exists = versions.stream()
                .anyMatch(v -> v.getVersionNumber().equals(version.getVersionNumber()));
        
        if (exists) {
            log.warn("版本已存在，将覆盖：agentId={}, version={}", 
                    this.agentId, version.getVersionNumber());
            // 移除旧版本
            versions.removeIf(v -> v.getVersionNumber().equals(version.getVersionNumber()));
        }
        
        versions.add(version);
        log.info("版本已添加：agentId={}, version={}", this.agentId, version.getVersionNumber());
    }
    
    /**
     * 设置当前版本
     * @param version 版本号
     * @return true-设置成功，false-版本不存在
     */
    public boolean setCurrentVersion(String version) {
        AgentVersion agentVersion = getVersion(version);
        if (agentVersion == null) {
            log.warn("版本不存在，无法设置为当前版本：agentId={}, version={}", 
                    this.agentId, version);
            return false;
        }
        
        this.currentVersion = version;
        log.info("当前版本已更新：agentId={}, version={}", this.agentId, version);
        return true;
    }
}
