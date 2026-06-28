package com.shiyu.ai.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.record.ProfileMemberDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人物成员关系表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface ProfileMemberMapper extends BaseMapperFlex<ProfileMemberDO> {

}
