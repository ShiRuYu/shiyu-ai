package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseChapterMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseChapterRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .CourseChapterRepository {

    @Resource private CourseChapterMapper courseChapterMapper;

    public List<CourseChapterBO> selectByCourseId(TenantId tenantId, Long courseId) {
        return MapstructUtils.convert(
                courseChapterMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("course_id", courseId)
                                .orderBy("order_no", true)),
                CourseChapterBO.class);
    }
}
