package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudyRecordDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudyRecordMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code StudyRecordRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class StudyRecordRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .StudyRecordRepository {

    /**
     * studyRecordMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private StudyRecordMapper studyRecordMapper;

    public List<StudyRecordBO> selectByStudent(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(
                studyRecordMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("student_id", studentId)
                                .orderBy("create_time", false)),
                StudyRecordBO.class);
    }

    public List<StudyRecordBO> selectByStudentAndKnowledge(
            TenantId tenantId, Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(
                studyRecordMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("student_id", studentId)
                                .eq("knowledge_id", knowledgeId)
                                .orderBy("create_time", false)),
                StudyRecordBO.class);
    }

    public int insert(TenantId tenantId, StudyRecordBO record) {
        StudyRecordDO dataObj = MapstructUtils.convert(record, StudyRecordDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows =
                EducationWriteGuard.require(
                        studyRecordMapper.insert(dataObj), "insert study record");
        record.setId(dataObj.getId());
        return rows;
    }
}
