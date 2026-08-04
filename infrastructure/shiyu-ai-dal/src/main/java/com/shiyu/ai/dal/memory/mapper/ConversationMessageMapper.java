package com.shiyu.ai.dal.memory.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.memory.dataobject.ConversationMessageDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Conversation Message 接口
 */

public interface ConversationMessageMapper extends BaseMapperFlex<ConversationMessageDO> {
}
