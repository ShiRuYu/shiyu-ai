package com.shiyu.ai.auth.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleScopeAuthCodeMapper extends BaseMapperFlex<RoleScopeAuthCodeDO> {
}

