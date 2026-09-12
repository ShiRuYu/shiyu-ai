package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * StudyRecordRepository 仓储接口，负责访问和持久化教育领域聚合数据。
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
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param record 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, StudyRecordBO record);
}
