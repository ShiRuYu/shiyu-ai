package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysDeptDO;
import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门管理 数据层
 *
 */
@Mapper
public interface SysDeptMapper extends BaseMapperFlex<SysDeptDO, SysDeptBO> {

}
