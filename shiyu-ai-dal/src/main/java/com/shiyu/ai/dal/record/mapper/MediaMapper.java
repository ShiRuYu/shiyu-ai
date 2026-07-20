package com.shiyu.ai.dal.record.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.record.dataobject.MediaDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Media 接口
 */

/**
 * 附件表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface MediaMapper extends BaseMapperFlex<MediaDO> {

}
