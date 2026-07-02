package com.shiyu.ai.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.record.TimelineEventDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Timeline Event 接口
 */

/**
 * 时间轴事件表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface TimelineEventMapper extends BaseMapperFlex<TimelineEventDO> {

}
