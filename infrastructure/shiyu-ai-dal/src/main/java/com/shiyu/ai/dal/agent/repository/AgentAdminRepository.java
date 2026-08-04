package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.agent.dataobject.AgentDefDO;
import com.shiyu.ai.dal.agent.dataobject.AgentVersionDO;
import com.shiyu.ai.dal.agent.mapper.AgentDefMapper;
import com.shiyu.ai.dal.agent.mapper.AgentVersionMapper;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentAdminRepository implements com.shiyu.ai.agent.port.repository.AgentAdminRepository {

    @Resource
    private AgentDefMapper agentDefMapper;

    @Resource
    private AgentVersionMapper agentVersionMapper;

    public Pair<Long, List<AgentDefBO>> selectPage(Number pageNo, Number pageSize, String name, Integer status) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AgentDefDO::getDelFlag, 0);
        if (StringUtils.isNotBlank(name)) {
            countWrapper.like(AgentDefDO::getName, name);
        }
        if (status != null) {
            countWrapper.eq(AgentDefDO::getStatus, status);
        }
        long count = agentDefMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(AgentDefDO::getName, name);
        }
        if (status != null) {
            queryWrapper.eq(AgentDefDO::getStatus, status);
        }
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        List<AgentDefDO> doList = agentDefMapper.selectListByQuery(queryWrapper);
        List<AgentDefBO> boList = MapstructUtils.convert(doList, AgentDefBO.class);
        return Pair.of(count, boList);
    }

    public AgentDefBO selectById(Long id) {
        AgentDefDO d = agentDefMapper.selectOneById(id);
        return MapstructUtils.convert(d, AgentDefBO.class);
    }

    public AgentDefBO selectByAgentId(String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getAgentId, agentId);
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        AgentDefDO d = agentDefMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(d, AgentDefBO.class);
    }

    public List<AgentDefBO> selectAllActive() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        queryWrapper.eq(AgentDefDO::getStatus, 1);
        queryWrapper.orderBy(AgentDefDO::getName, true);
        List<AgentDefDO> doList = agentDefMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(doList, AgentDefBO.class);
    }

    public AgentDefBO create(AgentDefBO agentDefBO) {
        AgentDefDO d = MapstructUtils.convert(agentDefBO, AgentDefDO.class);
        agentDefMapper.insertSelective(d);
        agentDefBO.setId(d.getId());
        return agentDefBO;
    }

    public AgentDefBO update(AgentDefBO agentDefBO) {
        AgentDefDO d = MapstructUtils.convert(agentDefBO, AgentDefDO.class);
        agentDefMapper.update(d);
        return agentDefBO;
    }

    public void deleteById(Long id) {
        AgentDefDO agentDef = agentDefMapper.selectOneById(id);
        if (agentDef != null) {
            agentDef.setDelFlag(1);
            agentDefMapper.update(agentDef);
        }
    }

    public void deleteByAgentId(String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getAgentId, agentId);
        queryWrapper.eq(AgentDefDO::getDelFlag, 0);
        AgentDefBO def = selectByAgentId(agentId);
        if (def != null) {
            deleteById(def.getId());
        }
    }

    public List<AgentVersionBO> selectVersionsByAgentId(String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getAgentId, agentId);
        queryWrapper.eq(AgentVersionDO::getDelFlag, 0);
        queryWrapper.orderBy(AgentVersionDO::getCreateTime, false);
        List<AgentVersionDO> doList = agentVersionMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(doList, AgentVersionBO.class);
    }

    public AgentVersionBO selectVersionById(Long versionId) {
        AgentVersionDO v = agentVersionMapper.selectOneById(versionId);
        return MapstructUtils.convert(v, AgentVersionBO.class);
    }

    public AgentVersionBO selectVersionByAgentIdAndNumber(String agentId, String versionNumber) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getAgentId, agentId);
        queryWrapper.eq(AgentVersionDO::getVersionNumber, versionNumber);
        queryWrapper.eq(AgentVersionDO::getDelFlag, 0);
        AgentVersionDO v = agentVersionMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(v, AgentVersionBO.class);
    }

    public AgentVersionBO createVersion(AgentVersionBO versionBO) {
        AgentVersionDO v = MapstructUtils.convert(versionBO, AgentVersionDO.class);
        agentVersionMapper.insertSelective(v);
        versionBO.setId(v.getId());
        return versionBO;
    }

    public AgentVersionBO updateVersion(AgentVersionBO versionBO) {
        AgentVersionDO v = MapstructUtils.convert(versionBO, AgentVersionDO.class);
        agentVersionMapper.update(v);
        return versionBO;
    }

    public void deleteVersionById(Long versionId) {
        AgentVersionDO version = agentVersionMapper.selectOneById(versionId);
        if (version != null) {
            version.setDelFlag(1);
            agentVersionMapper.update(version);
        }
    }
}
