package com.shiyu.ai.dal.mapper.knowledge;

import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;

import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {
}
