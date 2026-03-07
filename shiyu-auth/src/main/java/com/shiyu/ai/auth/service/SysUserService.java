package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.SysUserVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 用户服务层
 *
 * @author shiyu-ai
 */
public interface SysUserService {

    /**
     * 分页查询用户列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 用户列表
     */
    Pair<Long, List<SysUserVO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    SysUserVO getById(Long userId);

    /**
     * 创建用户
     *
     * @param sysUserBO 用户信息
     * @return 创建后的用户信息
     */
    SysUserVO create(SysUserBO sysUserBO);

    /**
     * 更新用户
     *
     * @param sysUserBO 用户信息
     * @return 更新后的用户信息
     */
    SysUserVO update(SysUserBO sysUserBO);

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     */
    void deleteById(Long userId);
}
