package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysDeptDO;
import com.shiyu.ai.demo.domain.bo.SysDeptBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门管理 数据层
 *
 */
@Mapper
public interface SysDeptMapper extends BaseMapperFlex<SysDeptDO, SysDeptBO> {

}
