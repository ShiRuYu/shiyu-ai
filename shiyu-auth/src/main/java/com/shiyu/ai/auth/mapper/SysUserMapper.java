package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysUserDO;
import com.shiyu.ai.auth.domain.bo.SysUserBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 *
 */
@Mapper
public interface SysUserMapper extends BaseMapperFlex<SysUserDO, SysUserBO> {


}
