package com.shiyu.ai.dal.bo.auth;

/**
 * 租户配额业务对象
 */
public class TenantQuotaBO {

    private Long id;
    private Long tenantId;
    private Long maxAgentCount;       // Agent 数量上限
    private Long maxTokenPerDay;      // 每日 Token 上限
    private Long maxStorageMb;        // 存储空间上限 (MB)
    private Long maxUserCount;        // 用户数量上限
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getMaxAgentCount() { return maxAgentCount; }
    public void setMaxAgentCount(Long maxAgentCount) { this.maxAgentCount = maxAgentCount; }

    public Long getMaxTokenPerDay() { return maxTokenPerDay; }
    public void setMaxTokenPerDay(Long maxTokenPerDay) { this.maxTokenPerDay = maxTokenPerDay; }

    public Long getMaxStorageMb() { return maxStorageMb; }
    public void setMaxStorageMb(Long maxStorageMb) { this.maxStorageMb = maxStorageMb; }

    public Long getMaxUserCount() { return maxUserCount; }
    public void setMaxUserCount(Long maxUserCount) { this.maxUserCount = maxUserCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
