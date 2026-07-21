package com.shiyu.ai.auth.quota;

import com.shiyu.ai.dal.auth.enums.TenantQuotaStatus;

import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.dal.auth.dataobject.TenantQuotaDO;
import com.shiyu.ai.dal.auth.bo.TenantQuotaBO;
import com.shiyu.ai.dal.auth.repository.TenantQuotaRepository;
import com.shiyu.ai.dal.agent.repository.AgentDefRepository;
import com.shiyu.ai.dal.agent.repository.UsageRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户配额服务
 * 提供租户资源配额的查询、校验和管理功能
 */
@Slf4j
@Service
public class TenantQuotaService {

    private final TenantQuotaRepository tenantQuotaRepository;
    private final AgentDefRepository agentDefRepository;
    private final UsageRecordRepository usageRecordRepository;

    public TenantQuotaService(TenantQuotaRepository tenantQuotaRepository,
                              AgentDefRepository agentDefRepository,
                              UsageRecordRepository usageRecordRepository) {
        this.tenantQuotaRepository = tenantQuotaRepository;
        this.agentDefRepository = agentDefRepository;
        this.usageRecordRepository = usageRecordRepository;
    }

    private TenantQuotaBO toBO(TenantQuotaDO doObj) {
        if (doObj == null) return null;
        TenantQuotaBO bo = new TenantQuotaBO();
        bo.setId(doObj.getId());
        bo.setTenantId(doObj.getTenantId());
        bo.setMaxAgentCount(doObj.getMaxAgentCount());
        bo.setMaxTokenPerDay(doObj.getMaxTokenPerDay());
        bo.setMaxStorageMb(doObj.getMaxStorageMb());
        bo.setMaxUserCount(doObj.getMaxUserCount());
        bo.setStatus(doObj.getStatus());
        return bo;
    }

    public TenantQuotaBO getQuota(Long tenantId) {
        return toBO(tenantQuotaRepository.getByTenantId(tenantId));
    }

    public void saveQuota(TenantQuotaBO quota) {
        TenantQuotaDO doObj = new TenantQuotaDO();
        doObj.setTenantId(quota.getTenantId());
        doObj.setMaxAgentCount(quota.getMaxAgentCount());
        doObj.setMaxTokenPerDay(quota.getMaxTokenPerDay());
        doObj.setMaxStorageMb(quota.getMaxStorageMb());
        doObj.setMaxUserCount(quota.getMaxUserCount());
        doObj.setStatus(quota.getStatus());
        tenantQuotaRepository.save(doObj);
    }

    public boolean checkCanCreateAgent(Long tenantId) {
        TenantQuotaBO quota = getQuota(tenantId);
        if (quota == null) return true;
        long count = agentDefRepository.countByTenantId(tenantId);
        return count < quota.getMaxAgentCount();
    }

    public boolean checkDailyTokenQuota(Long tenantId, int requestedTokens) {
        TenantQuotaBO quota = getQuota(tenantId);
        if (quota == null || quota.getMaxTokenPerDay() == null) return true;

        Long todayTokens = usageRecordRepository.sumLlmTodayTokensByTenantId(tenantId);
        return (todayTokens == null ? 0 : todayTokens) + requestedTokens <= quota.getMaxTokenPerDay();
    }

    public TenantQuotaBO getCurrentTenantQuota() {
        Long tenantId = LoginContextHolder.getTenantId();
        if (tenantId == null) return null;
        return getQuota(tenantId);
    }
}
