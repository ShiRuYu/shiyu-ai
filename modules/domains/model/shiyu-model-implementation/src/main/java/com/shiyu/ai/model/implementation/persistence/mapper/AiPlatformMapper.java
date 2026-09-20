package com.shiyu.ai.model.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * 负责 AI 平台 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface AiPlatformMapper extends BaseMapperFlex<AiPlatformDO> {}
