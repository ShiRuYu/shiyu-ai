package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeChunkDO;



/**
 * 负责 知识 Chunk 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {}
