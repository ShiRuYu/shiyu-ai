package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.DictDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典表 数据层
 */
@Mapper
public interface DictMapper extends BaseMapperFlex<DictDO> {

}
