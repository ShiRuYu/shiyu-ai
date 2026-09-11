package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeChunkDO;

import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {}
