package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.dal.mapper.education.AbilityMapper;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AbilityRepository {

    @Resource
    private AbilityMapper abilityMapper;

    public AbilityDO selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return abilityMapper.selectOneByQuery(
                QueryWrapper.create().eq("student_id", studentId).eq("knowledge_id", knowledgeId));
    }

    public List<AbilityDO> selectByStudent(Long studentId) {
        return abilityMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId));
    }

    public int insert(AbilityDO ability) {
        return abilityMapper.insert(ability);
    }

    public int update(AbilityDO ability) {
        return abilityMapper.update(ability);
    }
}
