package com.shiyu.ai.agent.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.record.ProfileMemberDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人物成员关系表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.RECORD)
public interface ProfileMemberMapper extends BaseMapperFlex<ProfileMemberDO> {

}
