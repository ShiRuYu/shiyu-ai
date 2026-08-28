package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AchievementBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.AchievementDO;
import com.shiyu.ai.education.implementation.persistence.mapper.AchievementMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AchievementRepositoryImpl implements com.shiyu.ai.education.port.repository.AchievementRepository {

    @Resource
    private AchievementMapper achievementMapper;

    public List<AchievementBO> selectByStudent(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(achievementMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("student_id", studentId).orderBy("earned_at", false)), AchievementBO.class);
    }

    public int insert(TenantId tenantId, AchievementBO a) {
        AchievementDO dataObj = MapstructUtils.convert(a, AchievementDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(achievementMapper.insert(dataObj), "insert achievement");
        a.setId(dataObj.getId());
        return rows;
    }
}

