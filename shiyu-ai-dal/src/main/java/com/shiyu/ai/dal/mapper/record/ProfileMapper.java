package com.shiyu.ai.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.record.ProfileDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Profile 接口
 */

/**
 * 人物表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface ProfileMapper extends BaseMapperFlex<ProfileDO> {

}
