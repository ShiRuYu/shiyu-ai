package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseSectionMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code CourseSectionRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class CourseSectionRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .CourseSectionRepository {

    /**
     * 课程小节映射器，表示当前对象中的对应属性。
     */
    @Resource private CourseSectionMapper courseSectionMapper;

    public List<CourseSectionBO> selectByChapterIds(TenantId tenantId, List<Long> chapterIds) {
        return MapstructUtils.convert(
                courseSectionMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .in("chapter_id", chapterIds)
                                .orderBy("order_no", true)),
                CourseSectionBO.class);
    }
}
