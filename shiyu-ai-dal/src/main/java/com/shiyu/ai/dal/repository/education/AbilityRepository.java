package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.AbilityBO;
import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.dal.mapper.education.AbilityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AbilityRepository {

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
        return abilityMapper.insert(dataObj);
    }

    public int update(AbilityBO ability) {
        AbilityDO dataObj = MapstructUtils.convert(ability, AbilityDO.class);
        return abilityMapper.update(dataObj);
    }
}
