package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.LongTermMemoryDO;
import com.shiyu.ai.agent.dal.mapper.agent.LongTermMemoryMapper;
import com.shiyu.ai.agent.domain.bo.LongTermMemoryBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LongTermMemoryRepository {

    @Resource
    private LongTermMemoryMapper longTermMemoryMapper;

    public void insert(LongTermMemoryBO bo) {
        LongTermMemoryDO memory = MapstructUtils.convert(bo, LongTermMemoryDO.class);
        longTermMemoryMapper.insertSelective(memory);
        bo.setId(memory.getId());
    }

    public List<LongTermMemoryBO> searchByKeyword(String keyword, Long userId, String agentId, int topK) {
        QueryWrapper qw = new QueryWrapper();
        if (userId != null) {
            qw.eq(LongTermMemoryDO::getUserId, userId);
        }
        if (agentId != null) {
            qw.eq(LongTermMemoryDO::getAgentId, agentId);
        }
        qw.like(LongTermMemoryDO::getContent, keyword);
        qw.orderBy(LongTermMemoryDO::getImportance, false);
        qw.limit(topK);
        List<LongTermMemoryDO> doList = longTermMemoryMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, LongTermMemoryBO.class);
    }

    public List<LongTermMemoryBO> searchByCategory(String category, Long userId, String agentId, int topK) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(LongTermMemoryDO::getCategory, category);
        if (userId != null) {
            qw.eq(LongTermMemoryDO::getUserId, userId);
        }
        if (agentId != null) {
            qw.eq(LongTermMemoryDO::getAgentId, agentId);
        }
        qw.orderBy(LongTermMemoryDO::getImportance, false);
        qw.limit(topK);
        List<LongTermMemoryDO> doList = longTermMemoryMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, LongTermMemoryBO.class);
    }

    public List<LongTermMemoryBO> selectTopByImportance(Long userId, String agentId, int topK) {
        QueryWrapper qw = new QueryWrapper();
        if (userId != null) {
            qw.eq(LongTermMemoryDO::getUserId, userId);
        }
        if (agentId != null) {
            qw.eq(LongTermMemoryDO::getAgentId, agentId);
        }
        qw.orderBy(LongTermMemoryDO::getImportance, false);
        qw.limit(topK);
        List<LongTermMemoryDO> doList = longTermMemoryMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, LongTermMemoryBO.class);
    }

    public void deleteById(Long id) {
        longTermMemoryMapper.deleteById(id);
    }
}
