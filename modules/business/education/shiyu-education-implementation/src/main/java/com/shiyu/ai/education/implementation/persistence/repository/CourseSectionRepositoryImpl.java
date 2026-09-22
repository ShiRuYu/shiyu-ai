package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseSectionMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 课程 Section 的持久化查询、保存和删除，并维护数据访问边界。
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
