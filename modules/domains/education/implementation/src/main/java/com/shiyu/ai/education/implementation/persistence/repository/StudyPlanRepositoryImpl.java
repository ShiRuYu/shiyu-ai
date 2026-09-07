package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudyPlanDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudyPlanMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanRepositoryImpl implements com.shiyu.ai.education.port.repository.StudyPlanRepository {

    @Resource
    private StudyPlanMapper studyPlanMapper;

    public StudyPlanBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(studyPlanMapper.selectOneByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), StudyPlanBO.class);
    }

    public List<StudyPlanBO> selectByStudentId(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(studyPlanMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .orderBy("create_time", false)
        ), StudyPlanBO.class);
    }

    public List<StudyPlanBO> selectActiveByStudent(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(studyPlanMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("status", 0)
        ), StudyPlanBO.class);
    }

    public int insert(TenantId tenantId, StudyPlanBO entity) {
        StudyPlanDO dataObj = MapstructUtils.convert(entity, StudyPlanDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(studyPlanMapper.insert(dataObj), "insert study plan");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, StudyPlanBO entity) {
        StudyPlanDO dataObj = MapstructUtils.convert(entity, StudyPlanDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(studyPlanMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", entity.getId())), "update study plan");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(studyPlanMapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "delete study plan");
    }

}

