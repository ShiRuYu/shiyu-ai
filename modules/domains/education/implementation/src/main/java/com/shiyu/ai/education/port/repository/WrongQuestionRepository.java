package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.WrongQuestionBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface WrongQuestionRepository {
    WrongQuestionBO selectById(TenantId tenantId, Long id);
    List<WrongQuestionBO> selectByStudentId(TenantId tenantId, Long studentId);
    WrongQuestionBO selectByStudentAndQuestion(TenantId tenantId, Long studentId, Long questionId);
    int insert(TenantId tenantId, WrongQuestionBO entity);
    int update(TenantId tenantId, WrongQuestionBO entity);
    int deleteById(TenantId tenantId, Long id);
}
