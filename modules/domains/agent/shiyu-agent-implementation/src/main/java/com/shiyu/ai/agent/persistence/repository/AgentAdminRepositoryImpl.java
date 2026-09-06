package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.persistence.dataobject.AgentDefDO;
import com.shiyu.ai.agent.persistence.dataobject.AgentVersionDO;
import com.shiyu.ai.agent.persistence.mapper.AgentDefMapper;
import com.shiyu.ai.agent.persistence.mapper.AgentVersionMapper;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentAdminRepositoryImpl implements com.shiyu.ai.agent.port.repository.AgentAdminRepository {

    @Resource
    private AgentDefMapper agentDefMapper;

    @Resource
    private AgentVersionMapper agentVersionMapper;

    @Override
    public Pair<Long, List<AgentDefBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, Integer status) {
        QueryWrapper count = QueryWrapper.create().eq(AgentDefDO::getTenantId, tenantId.value()).eq(AgentDefDO::getDelFlag, 0);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(name)) count.like(AgentDefDO::getName, name);
        if (status != null) count.eq(AgentDefDO::getStatus, status);
        long total = agentDefMapper.selectCountByQuery(count);
        QueryWrapper query = QueryWrapper.create().eq(AgentDefDO::getTenantId, tenantId.value()).eq(AgentDefDO::getDelFlag, 0);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(name)) query.like(AgentDefDO::getName, name);
        if (status != null) query.eq(AgentDefDO::getStatus, status);
        if (pageNo != null && pageSize != null) query.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        return Pair.of(total, MapstructUtils.convert(agentDefMapper.selectListByQuery(query), AgentDefBO.class));
    }

    @Override
    public AgentDefBO selectById(TenantId tenantId, Long id) {
        QueryWrapper query = QueryWrapper.create().eq(AgentDefDO::getTenantId, tenantId.value()).eq(AgentDefDO::getId, id).eq(AgentDefDO::getDelFlag, 0);
        return MapstructUtils.convert(agentDefMapper.selectOneByQuery(query), AgentDefBO.class);
    }

    @Override
    public AgentDefBO selectByAgentId(TenantId tenantId, String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentDefDO::getAgentId, agentId);
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        return MapstructUtils.convert(agentDefMapper.selectOneByQuery(queryWrapper), AgentDefBO.class);
    }

    @Override
    public List<AgentDefBO> selectAllActive(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        queryWrapper.eq(AgentDefDO::getStatus, 1);
        queryWrapper.orderBy(AgentDefDO::getName, true);
        return MapstructUtils.convert(agentDefMapper.selectListByQuery(queryWrapper), AgentDefBO.class);
    }

    @Override
    public AgentDefBO create(TenantId tenantId, AgentDefBO agentDefBO) {
        AgentDefDO data = MapstructUtils.convert(agentDefBO, AgentDefDO.class);
        data.setTenantId(tenantId.value());
        requireAffected(agentDefMapper.insertSelective(data), "create agent definition");
        agentDefBO.setTenantId(tenantId.value());
        agentDefBO.setId(data.getId());
        return agentDefBO;
    }

    @Override
    public AgentDefBO update(TenantId tenantId, AgentDefBO agentDefBO) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentDefDO::getId, agentDefBO.getId());
        if (agentDefMapper.selectOneByQuery(queryWrapper) == null) {
            throw new IllegalStateException("Agent definition does not exist in actor tenant");
        }
        AgentDefDO update = MapstructUtils.convert(agentDefBO, AgentDefDO.class);
        update.setTenantId(tenantId.value());
        requireAffected(agentDefMapper.update(update), "update agent definition");
        return agentDefBO;
    }

    @Override
    public void deleteById(TenantId tenantId, Long id) {
        QueryWrapper query = QueryWrapper.create().eq(AgentDefDO::getTenantId, tenantId.value()).eq(AgentDefDO::getId, id);
        AgentDefDO data = agentDefMapper.selectOneByQuery(query);
        if (data != null) { data.setDelFlag(1); requireAffected(agentDefMapper.update(data), "delete agent definition"); }
    }

    @Override
    public void deleteByAgentId(TenantId tenantId, String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentDefDO::getAgentId, agentId);
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        AgentDefDO definition = agentDefMapper.selectOneByQuery(queryWrapper);
        if (definition != null) {
            definition.setDelFlag(1);
            requireAffected(agentDefMapper.update(definition), "delete agent definition");
        }
    }

    @Override
    public List<AgentVersionBO> selectVersionsByAgentId(TenantId tenantId, String agentId) {
        QueryWrapper query = QueryWrapper.create().eq(AgentVersionDO::getTenantId, tenantId.value()).eq(AgentVersionDO::getAgentId, agentId).eq(AgentVersionDO::getDelFlag, 0).orderBy(AgentVersionDO::getCreateTime, false);
        return MapstructUtils.convert(agentVersionMapper.selectListByQuery(query), AgentVersionBO.class);
    }

    @Override
    public AgentVersionBO selectVersionById(TenantId tenantId, Long versionId) {
        QueryWrapper query = QueryWrapper.create().eq(AgentVersionDO::getTenantId, tenantId.value()).eq(AgentVersionDO::getId, versionId).eq(AgentVersionDO::getDelFlag, 0);
        return MapstructUtils.convert(agentVersionMapper.selectOneByQuery(query), AgentVersionBO.class);
    }

    @Override
    public AgentVersionBO selectVersionByAgentIdAndNumber(
            TenantId tenantId, String agentId, String versionNumber) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentVersionDO::getAgentId, agentId);
        queryWrapper.eq(AgentVersionDO::getVersionNumber, versionNumber);
        queryWrapper.eq(AgentVersionDO::getDelFlag, 0);
        return MapstructUtils.convert(agentVersionMapper.selectOneByQuery(queryWrapper), AgentVersionBO.class);
    }

    @Override
    public AgentVersionBO createVersion(TenantId tenantId, AgentVersionBO versionBO) {
        AgentVersionDO data = MapstructUtils.convert(versionBO, AgentVersionDO.class);
        data.setTenantId(tenantId.value());
        requireAffected(agentVersionMapper.insertSelective(data), "create agent version");
        versionBO.setTenantId(tenantId.value());
        versionBO.setId(data.getId());
        return versionBO;
    }

    @Override
    public AgentVersionBO updateVersion(TenantId tenantId, AgentVersionBO versionBO) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getTenantId, tenantId.value());
        queryWrapper.eq(AgentVersionDO::getId, versionBO.getId());
        if (agentVersionMapper.selectOneByQuery(queryWrapper) == null) {
            throw new IllegalStateException("Agent version does not exist in actor tenant");
        }
        AgentVersionDO update = MapstructUtils.convert(versionBO, AgentVersionDO.class);
        update.setTenantId(tenantId.value());
        requireAffected(agentVersionMapper.update(update), "update agent version");
        return versionBO;
    }

    @Override
    public void deleteVersionById(TenantId tenantId, Long versionId) {
        QueryWrapper query = QueryWrapper.create().eq(AgentVersionDO::getTenantId, tenantId.value()).eq(AgentVersionDO::getId, versionId);
        AgentVersionDO data = agentVersionMapper.selectOneByQuery(query);
        if (data != null) { data.setDelFlag(1); requireAffected(agentVersionMapper.update(data), "delete agent version"); }
    }

    private static void requireAffected(int rows, String operation) {
        if (rows < 1) {
            throw new IllegalStateException(operation + " affected no rows");
        }
    }
}
