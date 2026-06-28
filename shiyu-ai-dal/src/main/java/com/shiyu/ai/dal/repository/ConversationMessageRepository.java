package com.shiyu.ai.dal.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.agent.ConversationMessageDO;
import com.shiyu.ai.dal.mapper.agent.ConversationMessageMapper;
import com.shiyu.ai.model.bo.ConversationMessageBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMessageRepository {

    @Resource
    private ConversationMessageMapper conversationMessageMapper;

    public void insert(ConversationMessageBO bo) {
        ConversationMessageDO message = MapstructUtils.convert(bo, ConversationMessageDO.class);
        conversationMessageMapper.insertSelective(message);
        bo.setId(message.getId());
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
