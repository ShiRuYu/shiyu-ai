package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantModuleAccessDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 负责 租户 Module Access 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
public interface TenantModuleAccessMapper extends BaseMapperFlex<TenantModuleAccessDO> {}
