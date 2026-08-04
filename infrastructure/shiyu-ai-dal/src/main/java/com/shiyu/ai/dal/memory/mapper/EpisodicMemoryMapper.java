package com.shiyu.ai.dal.memory.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.memory.dataobject.EpisodicMemoryDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface EpisodicMemoryMapper extends BaseMapperFlex<EpisodicMemoryDO> {
}
