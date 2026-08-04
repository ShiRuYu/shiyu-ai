package com.shiyu.ai.dal.auth.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.auth.dataobject.MenuDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Menu 接口
 */

/**
 * 菜单表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface MenuMapper extends BaseMapperFlex<MenuDO> {

}
