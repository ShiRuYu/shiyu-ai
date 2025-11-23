package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysMenuDO;
import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表 数据层
 *
 */
@Mapper
public interface SysMenuMapper extends BaseMapperFlex<SysMenuDO, SysMenuBO> {

}
