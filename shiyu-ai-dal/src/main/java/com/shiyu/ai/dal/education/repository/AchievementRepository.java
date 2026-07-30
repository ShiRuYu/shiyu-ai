package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.AchievementBO;
import com.shiyu.ai.dal.education.dataobject.AchievementDO;
import com.shiyu.ai.dal.education.mapper.AchievementMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AchievementRepository {

    @Resource
    private AchievementMapper achievementMapper;

    public List<AchievementBO> selectByStudent(Long studentId) {
        return MapstructUtils.convert(achievementMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).orderBy("earned_at", false)), AchievementBO.class);
    }

    public int insert(AchievementBO a) {
        AchievementDO dataObj = MapstructUtils.convert(a, AchievementDO.class);
        int rows = achievementMapper.insert(dataObj);
        a.setId(dataObj.getId());
        return rows;
    }
}
