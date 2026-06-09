package com.shiyu.ai.agent.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.record.TimelineEventDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时间轴事件表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface TimelineEventMapper extends BaseMapperFlex<TimelineEventDO> {

}
