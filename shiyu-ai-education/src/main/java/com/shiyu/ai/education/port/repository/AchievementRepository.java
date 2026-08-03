package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.AchievementBO;
import java.util.List;

public interface AchievementRepository {
    List<AchievementBO> selectByStudent(Long studentId);
    int insert(AchievementBO a);
}
