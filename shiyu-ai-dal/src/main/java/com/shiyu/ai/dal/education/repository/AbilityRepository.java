package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.dal.education.dataobject.AbilityDO;
import com.shiyu.ai.dal.education.mapper.AbilityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AbilityRepository implements com.shiyu.ai.education.port.repository.AbilityRepository {

    @Resource
    private AbilityMapper abilityMapper;

    public AbilityBO selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(abilityMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
        ), AbilityBO.class);
    }

    public List<AbilityBO> selectByStudent(Long studentId) {
        return MapstructUtils.convert(abilityMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
        ), AbilityBO.class);
    }

    public int insert(AbilityBO ability) {
        AbilityDO dataObj = MapstructUtils.convert(ability, AbilityDO.class);
        int rows = abilityMapper.insert(dataObj);
        ability.setId(dataObj.getId());
        return rows;
    }

    public int update(AbilityBO ability) {
        AbilityDO dataObj = MapstructUtils.convert(ability, AbilityDO.class);
        return abilityMapper.update(dataObj);
    }
}
