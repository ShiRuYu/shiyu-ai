package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceDO;



/**
 * KnowledgeSpaceMapper 数据映射接口，负责在知识领域对象与持久化记录之间转换数据。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeSpaceMapper extends BaseMapperFlex<KnowledgeSpaceDO> {}
