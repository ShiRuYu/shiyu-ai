package com.shiyu.ai.record.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.record.implementation.persistence.dataobject.ProfileDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人物表 数据层
 */
@Mapper
@UseDataSource("agent")
public interface ProfileMapper extends BaseMapperFlex<ProfileDO> {

}
