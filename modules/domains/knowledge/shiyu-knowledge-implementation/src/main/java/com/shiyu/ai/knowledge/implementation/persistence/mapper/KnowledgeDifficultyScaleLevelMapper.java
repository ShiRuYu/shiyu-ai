package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDifficultyScaleLevelDO;



/**
 * 负责 知识 Difficulty Scale Level 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeDifficultyScaleLevelMapper
        extends BaseMapperFlex<KnowledgeDifficultyScaleLevelDO> {}
