package com.shiyu.ai.dal.memory.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.memory.dataobject.ConversationMessageDO;
import com.shiyu.ai.dal.memory.mapper.ConversationMessageMapper;
import com.shiyu.ai.memory.domain.model.ConversationMessageBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMessageRepository implements com.shiyu.ai.memory.port.repository.ConversationMessageRepository {

    @Resource
    private ConversationMessageMapper conversationMessageMapper;

    public void insert(ConversationMessageBO bo) {
        ConversationMessageDO message = MapstructUtils.convert(bo, ConversationMessageDO.class);
        conversationMessageMapper.insertSelective(message);
        bo.setId(message.getId());
    }

    public int deleteBySessionBefore(java.time.LocalDate deadline) {
        return conversationMessageMapper.deleteByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .lt("create_time", deadline.atStartOfDay()));
    }

    public List<ConversationMessageBO> selectRecentBySession(String sessionId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(ConversationMessageDO::getSessionId, sessionId);
        qw.orderBy(ConversationMessageDO::getCreateTime, false);
        qw.limit(limit);
        List<ConversationMessageDO> doList = conversationMessageMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, ConversationMessageBO.class);
    }

    public long countBySession(String sessionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(ConversationMessageDO::getSessionId, sessionId);
        return conversationMessageMapper.selectCountByQuery(qw);
    }

    public void deleteBySession(String sessionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(ConversationMessageDO::getSessionId, sessionId);
        conversationMessageMapper.deleteByQuery(qw);
    }
}
