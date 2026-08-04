package com.shiyu.ai.dal.auth.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleScopeAuthCodeMapper extends BaseMapperFlex<RoleScopeAuthCodeDO> {
}
