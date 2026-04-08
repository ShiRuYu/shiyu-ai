package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.MenuDO;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表 数据层
 */
@Mapper
public interface MenuMapper extends BaseMapperFlex<MenuDO> {


}
