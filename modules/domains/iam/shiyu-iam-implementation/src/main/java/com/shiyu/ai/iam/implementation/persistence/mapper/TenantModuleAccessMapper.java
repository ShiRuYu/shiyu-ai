package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantModuleAccessDO;
import org.apache.ibatis.annotations.Mapper;

/** 租户模块授权表 Mapper。 */
@Mapper
public interface TenantModuleAccessMapper extends BaseMapperFlex<TenantModuleAccessDO> {}
