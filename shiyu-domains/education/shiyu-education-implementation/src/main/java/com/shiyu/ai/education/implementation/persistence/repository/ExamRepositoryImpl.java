package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExamRepositoryImpl implements com.shiyu.ai.education.port.repository.ExamRepository {

    @Resource
    private ExamMapper examMapper;

    public ExamBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(examMapper.selectOneByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), ExamBO.class);
    }

    public PageData<ExamBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<ExamDO> page = examMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("tenant_id", tenantId.value())
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), ExamBO.class), page.getTotalRow());
    }

    public List<ExamBO> selectBySubjectCode(TenantId tenantId, String subjectCode) {
        return MapstructUtils.convert(examMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("subject_code", subjectCode)
        ), ExamBO.class);
    }

    public List<ExamBO> selectByTeacherId(TenantId tenantId, Long teacherId) {
        return MapstructUtils.convert(examMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("teacher_id", teacherId)
        ), ExamBO.class);
    }
    public List<ExamBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(examMapper.selectListByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())), ExamBO.class);
    }

    public int insert(TenantId tenantId, ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(examMapper.insert(dataObj), "insert exam");
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(TenantId tenantId, ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(examMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", entity.getId())), "update exam");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(examMapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "delete exam");
    }

}

