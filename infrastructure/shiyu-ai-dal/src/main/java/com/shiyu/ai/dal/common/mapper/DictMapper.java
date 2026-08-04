package com.shiyu.ai.dal.common.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.common.dataobject.DictDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dict 接口
 */

/**
 * 字典表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface DictMapper extends BaseMapperFlex<DictDO> {

}
