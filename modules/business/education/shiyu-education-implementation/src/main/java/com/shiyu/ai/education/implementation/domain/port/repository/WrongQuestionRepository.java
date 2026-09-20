package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Wrong 题目 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface WrongQuestionRepository {
    /**
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    WrongQuestionBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     *
     * @return 符合条件的结果集合。
     */
    List<WrongQuestionBO> selectByStudentId(TenantId tenantId, Long studentId);

    /**
     * 查询 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param studentId 用于定位student的标识。
     * @param questionId 用于定位question的标识。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    WrongQuestionBO selectByStudentAndQuestion(TenantId tenantId, Long studentId, Long questionId);

    /**
     * 创建或保存 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, WrongQuestionBO entity);

    /**
     * 更新或设置 Wrong 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 Wrong 题目 相关操作生成的结果数据。
     */
    int update(TenantId tenantId, WrongQuestionBO entity);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteById(TenantId tenantId, Long id);
}
