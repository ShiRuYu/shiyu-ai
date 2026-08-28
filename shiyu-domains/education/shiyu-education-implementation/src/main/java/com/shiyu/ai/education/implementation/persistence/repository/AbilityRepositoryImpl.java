package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.AbilityDO;
import com.shiyu.ai.education.implementation.persistence.mapper.AbilityMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AbilityRepositoryImpl implements com.shiyu.ai.education.port.repository.AbilityRepository {

    @Resource
    private AbilityMapper abilityMapper;

    public AbilityBO selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(abilityMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
        ), AbilityBO.class);
    }

    public List<AbilityBO> selectByStudent(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(abilityMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
        ), AbilityBO.class);
    }

    public int insert(TenantId tenantId, AbilityBO ability) {
        AbilityDO dataObj = MapstructUtils.convert(ability, AbilityDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(abilityMapper.insert(dataObj), "insert ability");
        ability.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, AbilityBO ability) {
        AbilityDO dataObj = MapstructUtils.convert(ability, AbilityDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(abilityMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", ability.getId())), "update ability");
    }
}

