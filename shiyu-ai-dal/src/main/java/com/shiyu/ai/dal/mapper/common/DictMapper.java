package com.shiyu.ai.dal.mapper.common;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.common.DictDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface DictMapper extends BaseMapperFlex<DictDO> {

}
