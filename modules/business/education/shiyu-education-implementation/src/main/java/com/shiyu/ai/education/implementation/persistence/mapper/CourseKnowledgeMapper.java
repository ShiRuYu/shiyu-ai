package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.CourseKnowledgeDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * CourseKnowledgeMapper 数据映射接口，负责在教育领域对象与持久化记录之间转换数据。
 */
@Mapper
@UseDataSource("agent")
public interface CourseKnowledgeMapper extends BaseMapperFlex<CourseKnowledgeDO> {}
