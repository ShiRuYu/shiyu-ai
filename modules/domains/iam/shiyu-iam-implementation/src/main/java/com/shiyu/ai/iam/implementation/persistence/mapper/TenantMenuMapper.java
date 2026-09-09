package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantMenuDO;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface TenantMenuMapper extends BaseMapperFlex<TenantMenuDO> {
}


