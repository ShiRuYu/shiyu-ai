package com.shiyu.ai.knowledge.repository;

import com.shiyu.ai.dal.dataobject.knowledge.AbilityDO;
import com.shiyu.ai.dal.mapper.knowledge.AbilityMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.shiyu.ai.dal.dataobject.knowledge.AbilityDO.*;

@Component
public class AbilityRepository {

    @Resource
    private AbilityMapper abilityMapper;

    public AbilityDO selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return abilityMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId));
    }

    public int insert(AbilityDO ability) {
        return abilityMapper.insert(ability);
    }

    public int update(AbilityDO ability) {
        return abilityMapper.update(ability);
    }
}
