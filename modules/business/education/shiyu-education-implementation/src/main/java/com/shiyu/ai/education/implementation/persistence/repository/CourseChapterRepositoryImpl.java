package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseChapterMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code CourseChapterRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class CourseChapterRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .CourseChapterRepository {

    /**
     * 课程章节映射器，表示当前对象中的对应属性。
     */
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
