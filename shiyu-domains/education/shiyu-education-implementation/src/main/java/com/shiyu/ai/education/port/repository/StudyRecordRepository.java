package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface StudyRecordRepository {
    List<StudyRecordBO> selectByStudent(TenantId tenantId, Long studentId);
    List<StudyRecordBO> selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId);
    int insert(TenantId tenantId, StudyRecordBO record);
}
