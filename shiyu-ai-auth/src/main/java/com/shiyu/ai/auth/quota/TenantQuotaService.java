package com.shiyu.ai.auth.quota;

import com.shiyu.ai.dal.bo.auth.TenantQuotaBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 租户配额服务
 * 提供租户资源配额的查询、校验和管理功能
 */
@Slf4j
@Service
public class TenantQuotaService {

    private final JdbcTemplate jdbcTemplate;

    public TenantQuotaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取租户配额
     */
    public TenantQuotaBO getQuota(Long tenantId) {
        List<TenantQuotaBO> results = jdbcTemplate.query(
            "SELECT * FROM tenant_quota WHERE tenant_id = ?",
            new TenantQuotaRowMapper(), tenantId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 创建或更新租户配额
     */
    public void saveQuota(TenantQuotaBO quota) {
        TenantQuotaBO existing = getQuota(quota.getTenantId());
        if (existing != null) {
            jdbcTemplate.update(
                "UPDATE tenant_quota SET max_agent_count=?, max_token_per_day=?, " +
                "max_storage_mb=?, max_user_count=?, status=? WHERE tenant_id=?",
                quota.getMaxAgentCount(), quota.getMaxTokenPerDay(),
                quota.getMaxStorageMb(), quota.getMaxUserCount(),
                quota.getStatus(), quota.getTenantId()
            );
        } else {
            jdbcTemplate.update(
                "INSERT INTO tenant_quota (tenant_id, max_agent_count, max_token_per_day, " +
                "max_storage_mb, max_user_count, status) VALUES (?, ?, ?, ?, ?, ?)",
                quota.getTenantId(), quota.getMaxAgentCount(), quota.getMaxTokenPerDay(),
                quota.getMaxStorageMb(), quota.getMaxUserCount(), quota.getStatus()
            );
        }
    }

    /**
     * 校验是否可创建 Agent
     */
    public boolean checkCanCreateAgent(Long tenantId) {
        TenantQuotaBO quota = getQuota(tenantId);
        if (quota == null) return true;
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM agent_def WHERE tenant_id = ? AND del_flag = 0",
            Long.class, tenantId
        );
        return count == null || count < quota.getMaxAgentCount();
    }

    /**
     * 校验每日 Token 限额
     */
    public boolean checkDailyTokenQuota(Long tenantId, int requestedTokens) {
        TenantQuotaBO quota = getQuota(tenantId);
        if (quota == null || quota.getMaxTokenPerDay() == null) return true;

        Long todayTokens = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(total_tokens), 0) FROM token_usage " +
            "WHERE user_id IN (SELECT id FROM auth_user WHERE tenant_id = ?) " +
            "AND create_time >= CURRENT_DATE",
            Long.class, tenantId
        );
        return (todayTokens == null ? 0 : todayTokens) + requestedTokens <= quota.getMaxTokenPerDay();
    }

    /**
     * 获取当前租户配额
     */
    public TenantQuotaBO getCurrentTenantQuota() {
        Long tenantId = LoginContextHolder.getTenantId();
        if (tenantId == null) return null;
        return getQuota(tenantId);
    }

    /**
     * 获取所有租户配额列表
     */
    public List<Map<String, Object>> listAllQuotas() {
        return jdbcTemplate.queryForList(
            "SELECT tq.*, t.name as tenant_name FROM tenant_quota tq " +
            "LEFT JOIN auth_tenant t ON tq.tenant_id = t.id " +
            "ORDER BY tq.tenant_id"
        );
    }

    private static class TenantQuotaRowMapper implements RowMapper<TenantQuotaBO> {
        @Override
        public TenantQuotaBO mapRow(ResultSet rs, int rowNum) throws SQLException {
            TenantQuotaBO bo = new TenantQuotaBO();
            bo.setId(rs.getLong("id"));
            bo.setTenantId(rs.getLong("tenant_id"));
            bo.setMaxAgentCount(rs.getLong("max_agent_count"));
            bo.setMaxTokenPerDay(rs.getLong("max_token_per_day"));
            bo.setMaxStorageMb(rs.getLong("max_storage_mb"));
            bo.setMaxUserCount(rs.getLong("max_user_count"));
            bo.setStatus(rs.getString("status"));
            return bo;
        }
    }
}
