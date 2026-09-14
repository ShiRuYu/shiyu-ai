package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeChunkDO;



/**
 * KnowledgeChunkMapper 数据映射接口，负责在知识领域对象与持久化记录之间转换数据。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {}
