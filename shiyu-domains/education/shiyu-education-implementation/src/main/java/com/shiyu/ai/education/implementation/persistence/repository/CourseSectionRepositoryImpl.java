package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseSectionBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.CourseSectionDO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseSectionMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseSectionRepositoryImpl implements com.shiyu.ai.education.port.repository.CourseSectionRepository {

    @Resource
    private CourseSectionMapper courseSectionMapper;

    public List<CourseSectionBO> selectByChapterIds(TenantId tenantId, List<Long> chapterIds) {
        return MapstructUtils.convert(courseSectionMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).in("chapter_id", chapterIds).orderBy("order_no", true)), CourseSectionBO.class);
    }
}

