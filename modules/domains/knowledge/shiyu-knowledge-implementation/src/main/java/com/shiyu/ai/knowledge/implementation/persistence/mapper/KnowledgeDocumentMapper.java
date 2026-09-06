package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO;
import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.annotation.UseDataSource;


@Mapper
@UseDataSource("agent")
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentDO> {
}

