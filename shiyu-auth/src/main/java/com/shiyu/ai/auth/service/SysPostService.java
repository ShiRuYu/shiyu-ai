package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysPostBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 岗位服务层
 *
 * @author shiyu-ai
 */
public interface SysPostService {

    /**
     * 分页查询岗位列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 岗位列表
     */
    Pair<Long, List<SysPostBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询岗位
     *
     * @param postId 岗位 ID
     * @return 岗位信息
     */
    SysPostBO getById(Long postId);

    /**
     * 创建岗位
     *
     * @param sysPostBO 岗位信息
     * @return 创建后的岗位信息
     */
    SysPostBO create(SysPostBO sysPostBO);

    /**
     * 更新岗位
     *
     * @param sysPostBO 岗位信息
     * @return 更新后的岗位信息
     */
    SysPostBO update(SysPostBO sysPostBO);

    /**
     * 删除岗位
     *
     * @param postId 岗位 ID
     */
    void deleteById(Long postId);
}
