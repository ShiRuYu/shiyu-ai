package com.shiyu.ai.iam.implementation.service.convert;

import com.shiyu.ai.iam.implementation.domain.model.DictBO;
import com.shiyu.ai.iam.implementation.vo.DictVO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 将 Dict 在不同层之间进行适配、转换或组装。
 */
@Mapper
public interface DictConverter {

    DictConverter INSTANCE = Mappers.getMapper(DictConverter.class);

    /**
     * 构建或转换 Dict 相关业务数据，并返回处理结果。
     *
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictVO toVO(DictBO bo);

    /**
     * 构建或转换 Dict 相关业务数据，并返回处理结果。
     *
     * @param boList 用于完成本次业务处理的 boList 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<DictVO> toVOList(List<DictBO> boList);
}
