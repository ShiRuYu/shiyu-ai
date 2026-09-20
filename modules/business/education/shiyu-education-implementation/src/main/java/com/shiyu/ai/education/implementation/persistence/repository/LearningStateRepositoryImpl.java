package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.LearningStateBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.LearningStateDO;
import com.shiyu.ai.education.implementation.persistence.mapper.LearningStateMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 负责 Learning State 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class LearningStateRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .LearningStateRepository {

    /**
     * learningStateMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private LearningStateMapper learningStateMapper;

    public LearningStateBO selectByStudentAndKnowledge(
            TenantId tenantId, Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(
                learningStateMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("student_id", studentId)
                                .eq("knowledge_id", knowledgeId)),
                LearningStateBO.class);
    }

    public int upsert(TenantId tenantId, LearningStateBO state) {
        LearningStateBO existing =
                selectByStudentAndKnowledge(tenantId, state.getStudentId(), state.getKnowledgeId());
        if (existing != null) {
            state.setId(existing.getId());
            LearningStateDO dataObj = MapstructUtils.convert(state, LearningStateDO.class);
            dataObj.setTenantId(tenantId.value());
            return EducationWriteGuard.require(
                    learningStateMapper.updateByQuery(
                            dataObj,
                            QueryWrapper.create()
                                    .eq("tenant_id", tenantId.value())
                                    .eq("id", state.getId())),
                    "update learning state");
        }
        LearningStateDO dataObj = MapstructUtils.convert(state, LearningStateDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(
                learningStateMapper.insert(dataObj), "insert learning state");
    }
}
