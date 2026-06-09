package com.shiyu.ai.agent.biz.agent.repository;

import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.IntentDefDO;
import com.shiyu.ai.agent.dal.mapper.agent.IntentDefMapper;
import com.shiyu.ai.agent.domain.bo.IntentDefBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class IntentDefRepository {

    @Resource
    private IntentDefMapper intentDefMapper;

    public Pair<Long, List<IntentDefBO>> selectPage(Number pageNo, Number pageSize, String agentId, String category) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(IntentDefDO::getDelFlag, "0");
        if (agentId != null) countWrapper.eq(IntentDefDO::getAgentId, agentId);
        if (category != null) countWrapper.eq(IntentDefDO::getCategory, category);
        long count = intentDefMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(IntentDefDO::getDelFlag, "0");
        if (agentId != null) queryWrapper.eq(IntentDefDO::getAgentId, agentId);
        if (category != null) queryWrapper.eq(IntentDefDO::getCategory, category);
        queryWrapper.orderBy(IntentDefDO::getPriority, true);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<IntentDefDO> doList = intentDefMapper.selectListByQuery(queryWrapper);
        List<IntentDefBO> boList = new ArrayList<>(doList.size());
        for (IntentDefDO d : doList) {
            boList.add(convertToBo(d));
        }
        return Pair.of(count, boList);
    }

    public List<IntentDefBO> selectByAgentId(String agentId) {
        // WHERE del_flag='0' AND enabled='1' AND status='1' AND (agent_id=? OR agent_id='default')
        QueryWrapper qw = new QueryWrapper();
        qw.eq(IntentDefDO::getDelFlag, "0")
          .eq(IntentDefDO::getEnabled, "1")
          .eq(IntentDefDO::getStatus, "1")
          .and("(agent_id = ? OR agent_id = 'default')", agentId)
          .orderBy(IntentDefDO::getPriority, true);
        List<IntentDefDO> doList = intentDefMapper.selectListByQuery(qw);
        List<IntentDefBO> boList = new ArrayList<>(doList.size());
        for (IntentDefDO d : doList) {
            boList.add(convertToBo(d));
        }
        return boList;
    }

    public List<IntentDefBO> selectByCategory(String agentId, String category) {
        // WHERE del_flag='0' AND enabled='1' AND status='1' AND category=? AND (agent_id=? OR agent_id='default')
        QueryWrapper qw = new QueryWrapper();
        qw.eq(IntentDefDO::getDelFlag, "0")
          .eq(IntentDefDO::getEnabled, "1")
          .eq(IntentDefDO::getStatus, "1")
          .eq(IntentDefDO::getCategory, category)
          .and("(agent_id = ? OR agent_id = 'default')", agentId)
          .orderBy(IntentDefDO::getPriority, true);
        List<IntentDefDO> doList = intentDefMapper.selectListByQuery(qw);
        List<IntentDefBO> boList = new ArrayList<>(doList.size());
        for (IntentDefDO d : doList) {
            boList.add(convertToBo(d));
        }
        return boList;
    }

    public IntentDefBO selectById(Long id) {
        IntentDefDO intentDO = intentDefMapper.selectOneById(id);
        if (intentDO == null) return null;
        return convertToBo(intentDO);
    }

    public IntentDefBO create(IntentDefBO bo) {
        IntentDefDO intentDO = new IntentDefDO();
        copyToDo(bo, intentDO);
        intentDefMapper.insertSelective(intentDO);
        bo.setId(intentDO.getId());
        return bo;
    }

    public IntentDefBO update(IntentDefBO bo) {
        IntentDefDO intentDO = new IntentDefDO();
        copyToDo(bo, intentDO);
        intentDO.setId(bo.getId());
        intentDefMapper.update(intentDO);
        return bo;
    }

    public void deleteById(Long id) {
        intentDefMapper.deleteById(id);
    }

    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            intentDefMapper.deleteById(id);
        }
    }

    private IntentDefBO convertToBo(IntentDefDO d) {
        IntentDefBO bo = new IntentDefBO();
        bo.setId(d.getId());
        bo.setAgentId(d.getAgentId());
        bo.setCode(d.getCode());
        bo.setName(d.getName());
        bo.setDescription(d.getDescription());
        bo.setCategory(d.getCategory());
        bo.setPriority(d.getPriority());
        bo.setConfidenceThreshold(d.getConfidenceThreshold());
        bo.setTargetNode(d.getTargetNode());
        bo.setStatus(d.getStatus());
        bo.setDelFlag(d.getDelFlag());
        bo.setCreateBy(d.getCreateBy());
        bo.setCreateTime(d.getCreateTime());
        bo.setUpdateBy(d.getUpdateBy());
        bo.setUpdateTime(d.getUpdateTime());
        // JSON fields
        if (d.getExamples() != null) {
            bo.setExamples(JSONUtil.toList(d.getExamples(), String.class));
        }
        if (d.getRequireSlotFilling() != null) {
            bo.setRequireSlotFilling("1".equals(d.getRequireSlotFilling()));
        }
        if (d.getEnabled() != null) {
            bo.setEnabled("1".equals(d.getEnabled()));
        }
        if (d.getSlots() != null) {
            bo.setSlots(JSONUtil.toBean(d.getSlots(), HashMap.class));
        }
        if (d.getParameterMapping() != null) {
            bo.setParameterMapping(JSONUtil.toBean(d.getParameterMapping(), HashMap.class));
        }
        if (d.getSlotDefaults() != null) {
            bo.setSlotDefaults(JSONUtil.toBean(d.getSlotDefaults(), HashMap.class));
        }
        return bo;
    }

    private void copyToDo(IntentDefBO bo, IntentDefDO d) {
        d.setAgentId(bo.getAgentId());
        d.setCode(bo.getCode());
        d.setName(bo.getName());
        d.setDescription(bo.getDescription());
        d.setCategory(bo.getCategory());
        d.setPriority(bo.getPriority());
        d.setConfidenceThreshold(bo.getConfidenceThreshold());
        d.setExamples(bo.getExamples() != null ? JSONUtil.toJsonStr(bo.getExamples()) : null);
        d.setTargetNode(bo.getTargetNode());
        d.setRequireSlotFilling(Boolean.TRUE.equals(bo.getRequireSlotFilling()) ? "1" : "0");
        d.setSlots(bo.getSlots() != null ? JSONUtil.toJsonStr(bo.getSlots()) : null);
        d.setParameterMapping(bo.getParameterMapping() != null ? JSONUtil.toJsonStr(bo.getParameterMapping()) : null);
        d.setSlotDefaults(bo.getSlotDefaults() != null ? JSONUtil.toJsonStr(bo.getSlotDefaults()) : null);
        d.setEnabled(Boolean.TRUE.equals(bo.getEnabled()) ? "1" : "0");
        d.setStatus("1");
        d.setDelFlag("0");
    }
}
