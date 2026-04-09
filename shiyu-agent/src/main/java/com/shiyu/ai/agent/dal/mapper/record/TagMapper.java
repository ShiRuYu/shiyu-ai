package com.shiyu.ai.agent.dal.mapper.record;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.record.TagDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.RECORD)
public interface TagMapper extends BaseMapperFlex<TagDO> {

}
