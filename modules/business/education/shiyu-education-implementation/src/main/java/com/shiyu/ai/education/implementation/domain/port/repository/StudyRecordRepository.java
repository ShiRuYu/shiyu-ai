package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Study Record 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface StudyRecordRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     *
     * @return 符合条件的结果集合。
     */
    List<StudyRecordBO> selectByStudent(TenantId tenantId, Long studentId);

    /** 根据租户、学生和幂等键查询练习提交记录。 */
    StudyRecordBO selectBySubmissionKey(TenantId tenantId, Long studentId, String key);

    /** 读取当前租户已评分的学习记录。 */
    StudyRecordBO selectById(TenantId tenantId, Long id);

    /** 原子地标记可信记录，确保其仅用于一次能力更新。 */
    int claimAnalysis(TenantId tenantId, Long studentId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     *
     * @return 符合条件的结果集合。
     */
    List<StudyRecordBO> selectByStudentAndKnowledge(
            TenantId tenantId, Long studentId, Long knowledgeId);

    /**
     * 创建或保存 Study Record 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param record 用于完成本次业务处理的 record 参数。
     * @return 返回 Study Record 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, StudyRecordBO record);
}
