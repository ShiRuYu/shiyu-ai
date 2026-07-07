package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.StudyPlanItemDO;
import com.shiyu.ai.dal.mapper.education.StudyPlanItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyPlanItemRepository {

    @Resource
    private StudyPlanItemMapper studyPlanItemMapper;

    public int insert(StudyPlanItemDO item) {
        return studyPlanItemMapper.insert(item);
    }

    public int insertBatch(List<StudyPlanItemDO> items) {
        int count = 0;
        for (StudyPlanItemDO item : items) {
            count += studyPlanItemMapper.insert(item);
        }
        return count;
    }

    public List<StudyPlanItemDO> selectByPlanId(Long planId) {
        return studyPlanItemMapper.selectListByQuery(
                QueryWrapper.create().eq("plan_id", planId).orderBy("order_no", true));
    }
}
