package com.shiyu.ai.dal.memory.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.memory.dataobject.EpisodicMemoryDO;
import com.shiyu.ai.dal.memory.mapper.EpisodicMemoryMapper;
import com.shiyu.ai.memory.domain.model.EpisodicMemoryBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EpisodicMemoryRepository implements com.shiyu.ai.memory.port.repository.EpisodicMemoryRepository {

    @Resource
    private EpisodicMemoryMapper episodicMemoryMapper;

    public void insert(EpisodicMemoryBO bo) {
        EpisodicMemoryDO d = MapstructUtils.convert(bo, EpisodicMemoryDO.class);
        episodicMemoryMapper.insertSelective(d);
        bo.setId(d.getId());
    }

    public List<EpisodicMemoryBO> selectByAgentId(String agentId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(EpisodicMemoryDO::getAgentId, agentId);
        qw.orderBy(EpisodicMemoryDO::getCreateTime, false);
        qw.limit(limit);
        List<EpisodicMemoryDO> list = episodicMemoryMapper.selectListByQuery(qw);
        return MapstructUtils.convert(list, EpisodicMemoryBO.class);
    }

    public List<EpisodicMemoryBO> selectByUserId(Long userId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(EpisodicMemoryDO::getUserId, userId);
        qw.orderBy(EpisodicMemoryDO::getCreateTime, false);
        qw.limit(limit);
        List<EpisodicMemoryDO> list = episodicMemoryMapper.selectListByQuery(qw);
        return MapstructUtils.convert(list, EpisodicMemoryBO.class);
    }
}
