package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 题目 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface QuestionRepository {
    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    QuestionBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    PageData<QuestionBO> selectPage(TenantId tenantId, int pageNum, int pageSize);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<QuestionBO> selectBySubjectAndGrade(TenantId tenantId, String subjectCode, Integer grade);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param difficulty 用于完成本次业务处理的 difficulty 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<QuestionBO> selectByDifficulty(TenantId tenantId, Integer difficulty);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<QuestionBO> selectByType(TenantId tenantId, String type);

    /**
     * 查询 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    QuestionBO selectByCode(TenantId tenantId, String code);

    /**
     * 执行 题目 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void incrementUsedCount(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<QuestionBO> selectAll(TenantId tenantId);

    /**
     * 创建或保存 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, QuestionBO entity);

    /**
     * 更新或设置 题目 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 题目 相关操作生成的结果数据。
     */
    int update(TenantId tenantId, QuestionBO entity);

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
