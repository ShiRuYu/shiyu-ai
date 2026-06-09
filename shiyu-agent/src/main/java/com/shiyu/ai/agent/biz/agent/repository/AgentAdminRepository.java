package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentDefDO;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentVersionDO;
import com.shiyu.ai.agent.dal.mapper.agent.AgentDefMapper;
import com.shiyu.ai.agent.dal.mapper.agent.AgentVersionMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentAdminRepository {

    @Resource
    private AgentDefMapper agentDefMapper;

    @Resource
    private AgentVersionMapper agentVersionMapper;

    public Pair<Long, List<AgentDefDO>> selectPage(Number pageNo, Number pageSize, String name, String status) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AgentDefDO::getDelFlag, "0");
        if (StringUtils.isNotBlank(name)) {
            countWrapper.like(AgentDefDO::getName, name);
        }
        if (StringUtils.isNotBlank(status)) {
            countWrapper.eq(AgentDefDO::getStatus, status);
        }
        long count = agentDefMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getDelFlag, "0");
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(AgentDefDO::getName, name);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(AgentDefDO::getStatus, status);
        }
        queryWrapper.orderBy(AgentDefDO::getUpdateTime, false);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        List<AgentDefDO> list = agentDefMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, list);
    }

    public AgentDefDO selectById(Long id) {
        return agentDefMapper.selectOneById(id);
    }

    public AgentDefDO selectByAgentId(String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getAgentId, agentId);
        queryWrapper.eq(AgentDefDO::getDelFlag, "0");
        return agentDefMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 查询所有启用 Agent（用于下拉选择）
     */
    public List<AgentDefDO> selectAllActive() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentDefDO::getDelFlag, "0");
        queryWrapper.eq(AgentDefDO::getStatus, "1");
        queryWrapper.orderBy(AgentDefDO::getName, true);
        return agentDefMapper.selectListByQuery(queryWrapper);
    }

    public AgentDefDO create(AgentDefDO agentDef) {
        agentDefMapper.insertSelective(agentDef);
        return agentDef;
    }

    public AgentDefDO update(AgentDefDO agentDef) {
        agentDefMapper.update(agentDef);
        return agentDef;
    }

    public void deleteById(Long id) {
        AgentDefDO agentDef = agentDefMapper.selectOneById(id);
        if (agentDef != null) {
            agentDef.setDelFlag("1");
            agentDefMapper.update(agentDef);
        }
    }

    public List<AgentVersionDO> selectVersionsByAgentId(String agentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getAgentId, agentId);
        queryWrapper.eq(AgentVersionDO::getDelFlag, "0");
        queryWrapper.orderBy(AgentVersionDO::getCreateTime, false);
        return agentVersionMapper.selectListByQuery(queryWrapper);
    }

    public AgentVersionDO selectVersionById(Long versionId) {
        return agentVersionMapper.selectOneById(versionId);
    }

    public AgentVersionDO selectVersionByAgentIdAndNumber(String agentId, String versionNumber) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AgentVersionDO::getAgentId, agentId);
        queryWrapper.eq(AgentVersionDO::getVersionNumber, versionNumber);
        queryWrapper.eq(AgentVersionDO::getDelFlag, "0");
        return agentVersionMapper.selectOneByQuery(queryWrapper);
    }

    public AgentVersionDO createVersion(AgentVersionDO version) {
        agentVersionMapper.insertSelective(version);
        return version;
    }

    public AgentVersionDO updateVersion(AgentVersionDO version) {
        agentVersionMapper.update(version);
        return version;
    }

    public void deleteVersionById(Long versionId) {
        AgentVersionDO version = agentVersionMapper.selectOneById(versionId);
        if (version != null) {
            version.setDelFlag("1");
            agentVersionMapper.update(version);
        }
    }
}
