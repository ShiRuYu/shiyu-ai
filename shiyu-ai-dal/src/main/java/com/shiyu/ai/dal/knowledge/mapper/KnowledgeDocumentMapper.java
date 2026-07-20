package com.shiyu.ai.dal.knowledge.mapper;

import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentDO;
import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Knowledge Document 接口
 */

public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentDO> {
}
