package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysPostDO;
import com.shiyu.ai.auth.domain.bo.SysPostBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位信息 数据层
 *
 */
@Mapper
public interface SysPostMapper extends BaseMapperFlex<SysPostDO, SysPostBO> {

}
