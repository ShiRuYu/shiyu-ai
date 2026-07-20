package com.shiyu.ai.dal.memory.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.memory.dataobject.LongTermMemoryDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Long Term Memory 接口
 */

public interface LongTermMemoryMapper extends BaseMapperFlex<LongTermMemoryDO> {
}
