package com.shiyu.ai.dal.record.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.record.dataobject.TagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Tag 接口
 */

/**
 * 标签表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface TagMapper extends BaseMapperFlex<TagDO> {

}
