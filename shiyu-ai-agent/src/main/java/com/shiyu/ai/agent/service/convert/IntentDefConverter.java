package com.shiyu.ai.agent.service.convert;

import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.dal.agent.bo.IntentDefBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IntentDefBO → IntentDefVO 转换器
 */
@Mapper
public interface IntentDefConverter {

    IntentDefConverter INSTANCE = Mappers.getMapper(IntentDefConverter.class);

    IntentDefVO toVO(IntentDefBO bo);

    List<IntentDefVO> toVOList(List<IntentDefBO> boList);
}
