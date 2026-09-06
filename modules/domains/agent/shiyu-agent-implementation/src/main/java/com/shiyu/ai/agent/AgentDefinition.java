package com.shiyu.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDefinition {
    private String agentId;
    private String name;
    private String description;
    private Map<String, Object> extInfo;
    private String currentVersion;
    private long createdAt;
    private long updatedAt;
    private String startNodeId;

    @Builder.Default
    private Map<String, AgentVersion> versions = new HashMap<>();

    public void addVersion(AgentVersion version) {
        if (versions == null) {
            versions = new HashMap<>();
        }
        versions.put(version.getVersionNumber(), version);
    }

    public AgentVersion getVersion(String versionNumber) {
        if (versions == null) return null;
        if (versionNumber == null || versionNumber.isBlank()) {
            versionNumber = currentVersion;
        }
        return versionNumber != null ? versions.get(versionNumber) : null;
    }
}
