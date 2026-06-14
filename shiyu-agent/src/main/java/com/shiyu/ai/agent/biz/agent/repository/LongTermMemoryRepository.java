package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.LongTermMemoryDO;
import com.shiyu.ai.agent.dal.mapper.agent.LongTermMemoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LongTermMemoryRepository {

    @Resource
    private LongTermMemoryMapper longTermMemoryMapper;

    public void insert(LongTermMemoryDO memory) {
        longTermMemoryMapper.insertSelective(memory);
    }

    public List<LongTermMemoryDO> searchByKeyword(String keyword, Long userId, String agentId, int topK) {
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
        return longTermMemoryMapper.selectListByQuery(qw);
    }

    public List<LongTermMemoryDO> searchByCategory(String category, Long userId, String agentId, int topK) {
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
        return longTermMemoryMapper.selectListByQuery(qw);
    }

    public List<LongTermMemoryDO> selectTopByImportance(Long userId, String agentId, int topK) {
        QueryWrapper qw = new QueryWrapper();
        if (userId != null) {
            qw.eq(LongTermMemoryDO::getUserId, userId);
        }
        if (agentId != null) {
            qw.eq(LongTermMemoryDO::getAgentId, agentId);
        }
        qw.orderBy(LongTermMemoryDO::getImportance, false);
        qw.limit(topK);
        return longTermMemoryMapper.selectListByQuery(qw);
    }

    public void deleteById(Long id) {
        longTermMemoryMapper.deleteById(id);
    }
}
