package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 负责 考试 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class ExamRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository {

    /**
     * 考试映射器，表示当前对象中的对应属性。
     */
    @Resource private ExamMapper examMapper;

    public ExamBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(
                examMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", id)
                                .eq("del_flag", 0)),
                ExamBO.class);
    }

    @Override
    public List<ExamBO> selectByIds(TenantId tenantId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return MapstructUtils.convert(
                examMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .in("id", ids)
                                .eq("del_flag", 0)),
                ExamBO.class);
    }

    public PageData<ExamBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<ExamDO> page =
                examMapper.paginate(
                        pageNum,
                        pageSize,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .orderBy("id", false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), ExamBO.class), page.getTotalRow());
    }

    @Override
    public PageData<ExamBO> selectAvailable(TenantId tenantId, int grade,
            String subjectCode, LocalDateTime now, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .eq("tenant_id", tenantId.value())
                .eq("grade", grade)
                .eq("status", 1)
                .eq("del_flag", 0)
                .le("start_time", now)
                .gt("end_time", now)
                .orderBy("start_time", true);
        if (subjectCode != null && !subjectCode.isBlank()) {
            query.eq("subject_code", subjectCode);
        }
        var page = examMapper.paginate(pageNum, pageSize, query);
        return new PageData<>(MapstructUtils.convert(page.getRecords(), ExamBO.class),
                page.getTotalRow());
    }

    public List<ExamBO> selectBySubjectCode(TenantId tenantId, String subjectCode) {
        return MapstructUtils.convert(
                examMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("subject_code", subjectCode)),
                ExamBO.class);
    }

    public List<ExamBO> selectByTeacherId(TenantId tenantId, Long teacherId) {
        return MapstructUtils.convert(
                examMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("teacher_id", teacherId)),
                ExamBO.class);
    }

    public List<ExamBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(
                examMapper.selectListByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value())),
                ExamBO.class);
    }

    public int insert(TenantId tenantId, ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        dataObj.setTenantId(tenantId.value());
        dataObj.setDelFlag(0);
        int rows = EducationWriteGuard.require(examMapper.insert(dataObj), "insert exam");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(
                examMapper.updateByQuery(
                        dataObj,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", entity.getId())),
                "update exam");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(
                examMapper.deleteByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                "delete exam");
    }
}
