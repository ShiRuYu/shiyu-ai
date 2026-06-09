package com.shiyu.ai.agent.dal.mapper.common;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.common.DictDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface DictMapper extends BaseMapperFlex<DictDO> {

}
