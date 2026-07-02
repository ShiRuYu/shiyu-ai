package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserPageResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户详情
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    /**
     * Get User Detail
     * @return 处理结果
     */
    UserBO getUserDetail(Long userId);

    /**
     * 获取用户列表 - 分页
     *
     * @param username 用户名（可选）
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    /**
     * Get User List
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    UserPageResponse getUserList(String username, Number pageNo, Number pageSize);

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     * @return 是否成功
     */
    /**
     * Delete User
     * @return 处理结果
     */
    boolean deleteUser(Long userId);

    /**
     * 修改用户
     *
     * @param userId  用户 ID
     * @param userBO  用户信息
     * @return 是否成功
     */
    /**
     * Update User
     * @param UserBO UserBO
     * @return 处理结果
     */
    boolean updateUser(Long userId, UserBO userBO);

    /**
     * 重置用户密码
     *
     * @param userId  用户 ID
     * @param password 新密码
     * @return 是否成功
     */
    /**
     * Reset User Password
     * @return 处理结果
     */
    boolean resetUserPassword(Long userId, String password);

    /**
     * 新增用户
     *
     * @param userBO 用户信息
     * @return 用户 ID
     */
    /**
     * Create User
     * @param UserBO UserBO
     * @return 处理结果
     */
    Long createUser(UserBO userBO);

    /**
     * 修改密码（校验旧密码）
     *
     * @param userId      用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    /**
     * Change Password
     * @return 处理结果
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
