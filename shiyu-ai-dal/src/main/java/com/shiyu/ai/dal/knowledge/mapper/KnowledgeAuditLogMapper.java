package com.shiyu.ai.dal.knowledge.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface KnowledgeAuditLogMapper extends BaseMapperFlex<KnowledgeAuditLogDO> {
}
