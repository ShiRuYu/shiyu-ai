package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.AchievementDO;
import com.shiyu.ai.dal.mapper.education.AchievementMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AchievementRepository {
    @Resource private AchievementMapper achievementMapper;

    public List<AchievementDO> selectByStudent(Long studentId) {
        return achievementMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).orderBy("earned_at", false));
    }

    public int insert(AchievementDO a) { return achievementMapper.insert(a); }
}
