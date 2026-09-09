package com.shiyu.ai.iam.implementation.service.convert;

import com.shiyu.ai.iam.implementation.vo.DictVO;
import com.shiyu.ai.iam.implementation.domain.model.DictBO;
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

