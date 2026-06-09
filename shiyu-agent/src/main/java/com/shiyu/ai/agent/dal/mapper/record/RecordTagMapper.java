package com.shiyu.ai.agent.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.record.RecordTagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记录标签关联表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RecordTagMapper extends BaseMapperFlex<RecordTagDO> {

}
