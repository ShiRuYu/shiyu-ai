package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.CourseDO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseRepositoryImpl implements com.shiyu.ai.education.port.repository.CourseRepository {

    @Resource
    private CourseMapper courseMapper;

    public CourseBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(courseMapper.selectOneByQuery(QueryWrapper.create()
                .eq(CourseDO::getTenantId, tenantId.value()).eq(CourseDO::getId, id)), CourseBO.class);
    }

    public PageData<CourseBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<CourseDO> page = courseMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq(CourseDO::getTenantId, tenantId.value())
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), CourseBO.class), page.getTotalRow());
    }

    public List<CourseBO> selectBySubjectCode(TenantId tenantId, String subjectCode) {
        return MapstructUtils.convert(courseMapper.selectListByQuery(
                QueryWrapper.create().eq(CourseDO::getTenantId, tenantId.value())
                        .eq("subject_code", subjectCode)
        ), CourseBO.class);
    }

    public List<CourseBO> selectByGrade(TenantId tenantId, Integer grade) {
        return MapstructUtils.convert(courseMapper.selectListByQuery(
                QueryWrapper.create().eq(CourseDO::getTenantId, tenantId.value())
                        .eq("grade", grade)
        ), CourseBO.class);
    }
    public List<CourseBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(courseMapper.selectListByQuery(QueryWrapper.create()
                .eq(CourseDO::getTenantId, tenantId.value())), CourseBO.class);
    }

    public int insert(TenantId tenantId, CourseBO entity) {
        CourseDO dataObj = MapstructUtils.convert(entity, CourseDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(courseMapper.insert(dataObj), "insert course");
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(TenantId tenantId, CourseBO entity) {
        CourseDO dataObj = MapstructUtils.convert(entity, CourseDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(courseMapper.updateByQuery(dataObj, QueryWrapper.create()
                .eq(CourseDO::getTenantId, tenantId.value()).eq(CourseDO::getId, entity.getId())), "update course");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(courseMapper.deleteByQuery(QueryWrapper.create()
                .eq(CourseDO::getTenantId, tenantId.value()).eq(CourseDO::getId, id)), "delete course");
    }

}

