package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.UserDO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 */
@Mapper
public interface UserMapper extends BaseMapperFlex<UserDO, UserBO> {


}
