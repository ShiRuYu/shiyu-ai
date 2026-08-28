package com.shiyu.ai.record.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.record.implementation.persistence.dataobject.TagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签表 数据层
 */
@Mapper
@UseDataSource("agent")
public interface TagMapper extends BaseMapperFlex<TagDO> {

}
