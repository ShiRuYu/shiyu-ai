package com.shiyu.ai.auth.service.convert;

import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.dal.common.bo.DictBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * DictBO → DictVO 转换器
 */
@Mapper
public interface DictConverter {

    DictConverter INSTANCE = Mappers.getMapper(DictConverter.class);

    DictVO toVO(DictBO bo);

    List<DictVO> toVOList(List<DictBO> boList);
}
