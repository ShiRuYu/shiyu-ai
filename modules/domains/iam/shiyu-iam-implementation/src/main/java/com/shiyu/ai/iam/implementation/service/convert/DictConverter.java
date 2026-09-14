package com.shiyu.ai.iam.implementation.service.convert;

import com.shiyu.ai.iam.implementation.domain.model.DictBO;
import com.shiyu.ai.iam.implementation.vo.DictVO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** DictBO → DictVO 转换器 */
@Mapper
public interface DictConverter {

    DictConverter INSTANCE = Mappers.getMapper(DictConverter.class);

    /**
     * 执行 {@code toVO} 定义的接口操作。
     *
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    DictVO toVO(DictBO bo);

    /**
     * 执行 {@code toVOList} 定义的接口操作。
     *
     * @param boList 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<DictVO> toVOList(List<DictBO> boList);
}
