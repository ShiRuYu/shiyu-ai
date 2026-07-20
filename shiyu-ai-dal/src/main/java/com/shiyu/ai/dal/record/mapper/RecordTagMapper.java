package com.shiyu.ai.dal.record.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.record.dataobject.RecordTagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Record Tag 接口
 */

/**
 * 记录标签关联表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RecordTagMapper extends BaseMapperFlex<RecordTagDO> {

}
