package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysPostDO;
import com.shiyu.ai.demo.domain.bo.SysPostBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位信息 数据层
 *
 */
@Mapper
public interface SysPostMapper extends BaseMapperFlex<SysPostDO, SysPostBO> {

}
