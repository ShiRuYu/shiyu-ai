package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.ConversationMessageDO;
import com.shiyu.ai.agent.dal.mapper.agent.ConversationMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMessageRepository {

    @Resource
    private ConversationMessageMapper conversationMessageMapper;

    public void insert(ConversationMessageDO message) {
        conversationMessageMapper.insertSelective(message);
    }

    public List<ConversationMessageDO> selectRecentBySession(String sessionId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(ConversationMessageDO::getSessionId, sessionId);
        qw.orderBy(ConversationMessageDO::getCreateTime, false);
        qw.limit(limit);
        return conversationMessageMapper.selectListByQuery(qw);
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
