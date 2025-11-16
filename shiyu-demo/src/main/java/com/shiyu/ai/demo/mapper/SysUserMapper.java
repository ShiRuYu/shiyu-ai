package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysUserDO;
import com.shiyu.ai.demo.domain.bo.SysUserBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 *
 */
@Mapper
public interface SysUserMapper extends BaseMapperFlex<SysUserDO, SysUserBO> {


}
