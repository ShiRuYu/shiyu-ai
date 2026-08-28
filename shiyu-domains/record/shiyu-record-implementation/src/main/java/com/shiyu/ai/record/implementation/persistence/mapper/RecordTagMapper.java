package com.shiyu.ai.record.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.record.implementation.persistence.dataobject.RecordTagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记录标签关联表 数据层
 */
@Mapper
@UseDataSource("agent")
public interface RecordTagMapper extends BaseMapperFlex<RecordTagDO> {

}
