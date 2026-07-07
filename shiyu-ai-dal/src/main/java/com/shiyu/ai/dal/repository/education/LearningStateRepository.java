package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.LearningStateDO;
import com.shiyu.ai.dal.mapper.education.LearningStateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LearningStateRepository {

    @Resource
    private LearningStateMapper learningStateMapper;

    public LearningStateDO selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return learningStateMapper.selectOneByQuery(
                QueryWrapper.create().eq("student_id", studentId).eq("knowledge_id", knowledgeId));
    }

    public int upsert(LearningStateDO state) {
        LearningStateDO existing = selectByStudentAndKnowledge(state.getStudentId(), state.getKnowledgeId());
        if (existing != null) {
            state.setId(existing.getId());
            state.setCreateTime(existing.getCreateTime());
            return learningStateMapper.update(state);
        }
        return learningStateMapper.insert(state);
    }
}
