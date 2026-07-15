package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDate;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.StudyPlanItemBO;
import com.shiyu.ai.dal.dataobject.education.StudyPlanItemDO;
import com.shiyu.ai.dal.mapper.education.StudyPlanItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanItemRepository {

    @Resource
    private StudyPlanItemMapper studyPlanItemMapper;

    public List<StudyPlanItemBO> selectByPlanId(Long planId) {
        return MapstructUtils.convert(studyPlanItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("plan_id", planId)
                        .orderBy("order_no", true)
        ), StudyPlanItemBO.class);
    }

    public List<StudyPlanItemBO> selectTodayItems(List<Long> planIds) {
        return MapstructUtils.convert(studyPlanItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .in("plan_id", planIds)
                        .eq("plan_date", LocalDate.now())
                        .orderBy("order_no", true)
        ), StudyPlanItemBO.class);
    }

    public int insertBatch(List<StudyPlanItemBO> items) {
        int count = 0;
        for (StudyPlanItemBO item : items) {
            StudyPlanItemDO dataObj = MapstructUtils.convert(item, StudyPlanItemDO.class);
            count += studyPlanItemMapper.insert(dataObj);
        }
        return count;
    }

}
