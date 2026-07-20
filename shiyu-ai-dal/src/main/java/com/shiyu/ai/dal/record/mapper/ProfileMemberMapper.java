package com.shiyu.ai.dal.record.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.record.dataobject.ProfileMemberDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Profile Member 接口
 */

/**
 * 人物成员关系表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface ProfileMemberMapper extends BaseMapperFlex<ProfileMemberDO> {

}
