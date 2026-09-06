package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudyRecordDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudyRecordMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyRecordRepositoryImpl implements com.shiyu.ai.education.port.repository.StudyRecordRepository {

    @Resource
    private StudyRecordMapper studyRecordMapper;

    public List<StudyRecordBO> selectByStudent(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(studyRecordMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .orderBy("create_time", false)
        ), StudyRecordBO.class);
    }

    public List<StudyRecordBO> selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(studyRecordMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("create_time", false)
        ), StudyRecordBO.class);
    }

    public int insert(TenantId tenantId, StudyRecordBO record) {
        StudyRecordDO dataObj = MapstructUtils.convert(record, StudyRecordDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(studyRecordMapper.insert(dataObj), "insert study record");
        record.setId(dataObj.getId());
        return rows;
    }

}

