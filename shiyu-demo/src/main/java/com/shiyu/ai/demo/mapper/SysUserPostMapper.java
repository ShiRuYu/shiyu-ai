package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysUserPostDO;

/**
 * 用户与岗位关联表 数据层
 *
 */
@Mapper
public interface SysUserPostMapper extends BaseMapperFlex<SysUserPostDO, SysUserPostDO> {

}
