package com.shiyu.ai.record.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.record.implementation.persistence.dataobject.ProfileMemberDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人物成员关系表 数据层
 */
@Mapper
@UseDataSource("agent")
public interface ProfileMemberMapper extends BaseMapperFlex<ProfileMemberDO> {

}
