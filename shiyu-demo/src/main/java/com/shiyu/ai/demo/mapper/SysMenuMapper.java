package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysMenuDO;
import com.shiyu.ai.demo.domain.bo.SysMenuBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表 数据层
 *
 */
@Mapper
public interface SysMenuMapper extends BaseMapperFlex<SysMenuDO, SysMenuBO> {

}
