package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysUserPostDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与岗位关联表 数据层
 *
 */
@Mapper
public interface SysUserPostMapper extends BaseMapperFlex<SysUserPostDO, SysUserPostDO> {

}
