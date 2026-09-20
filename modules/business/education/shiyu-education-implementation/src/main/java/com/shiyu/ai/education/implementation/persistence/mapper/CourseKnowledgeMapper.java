package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.CourseKnowledgeDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * 负责 课程 知识 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface CourseKnowledgeMapper extends BaseMapperFlex<CourseKnowledgeDO> {}
