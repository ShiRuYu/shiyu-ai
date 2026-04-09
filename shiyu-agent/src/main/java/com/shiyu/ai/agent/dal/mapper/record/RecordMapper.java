package com.shiyu.ai.agent.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.record.RecordDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记录内容表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.RECORD)
public interface RecordMapper extends BaseMapperFlex<RecordDO> {

}
