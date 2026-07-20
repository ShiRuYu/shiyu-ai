package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.common.bo.DictBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 字典服务层
 */
public interface DictService {

    /**
     * 分页查询字典列表
     *
     * @param pageNo 页码
     * @param pageSize   每页数量
     * @return 字典列表
     */
    /**
     * Get All
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    Pair<Long, List<DictBO>> getAll(Number pageNo, Number pageSize);

    /**
     * 根据ID查询字典
     *
     * @param id 字典ID
     * @return 字典信息
     */
    /**
     * Get By Id
     * @return 处理结果
     */
    DictBO getById(Long id);

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    /**
     * Get By Dict Type
     * @return 处理结果
     */
    List<DictBO> getByDictType(String dictType);

    /**
     * 创建字典
     *
     * @param dictBO 字典信息
     * @return 创建后的字典信息
     */
    /**
     * Create
     * @param DictBO DictBO
     * @return 处理结果
     */
    DictBO create(DictBO dictBO);

    /**
     * 更新字典
     *
     * @param dictBO 字典信息
     * @return 更新后的字典信息
     */
    /**
     * Update
     * @param DictBO DictBO
     * @return 处理结果
     */
    DictBO update(DictBO dictBO);

    /**
     * 删除字典
     *
     * @param id 字典ID
     */
    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);

    /**
     * 批量删除字典
     *
     * @param ids 字典ID列表
     */
    /**
     * Delete By Ids
     * @return 处理结果
     */
    void deleteByIds(List<Long> ids);
}
