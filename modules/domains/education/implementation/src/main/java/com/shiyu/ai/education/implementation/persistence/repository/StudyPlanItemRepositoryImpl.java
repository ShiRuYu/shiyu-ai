package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDate;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudyPlanItemBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudyPlanItemDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudyPlanItemMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanItemRepositoryImpl implements com.shiyu.ai.education.port.repository.StudyPlanItemRepository {

    @Resource
    private StudyPlanItemMapper studyPlanItemMapper;

    public List<StudyPlanItemBO> selectByPlanId(TenantId tenantId, Long planId) {
        return MapstructUtils.convert(studyPlanItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("plan_id", planId)
                        .orderBy("order_no", true)
        ), StudyPlanItemBO.class);
    }

    public List<StudyPlanItemBO> selectTodayItems(TenantId tenantId, List<Long> planIds) {
        return MapstructUtils.convert(studyPlanItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).in("plan_id", planIds)
                        .eq("plan_date", LocalDate.now())
                        .orderBy("order_no", true)
        ), StudyPlanItemBO.class);
    }

    public int insertBatch(TenantId tenantId, List<StudyPlanItemBO> items) {
        int count = 0;
        for (StudyPlanItemBO item : items) {
            StudyPlanItemDO dataObj = MapstructUtils.convert(item, StudyPlanItemDO.class);
            dataObj.setTenantId(tenantId.value());
            count += EducationWriteGuard.require(studyPlanItemMapper.insert(dataObj), "insert study plan item");
        }
        return count;
    }

}

