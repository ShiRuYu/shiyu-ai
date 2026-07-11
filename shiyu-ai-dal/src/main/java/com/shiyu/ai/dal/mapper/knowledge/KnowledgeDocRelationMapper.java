package com.shiyu.ai.dal.mapper.knowledge;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface KnowledgeDocRelationMapper extends BaseMapper<KnowledgeDocRelationDO> {
}