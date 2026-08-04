package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.LearningStateBO;
import com.shiyu.ai.dal.education.dataobject.LearningStateDO;
import com.shiyu.ai.dal.education.mapper.LearningStateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LearningStateRepository implements com.shiyu.ai.education.port.repository.LearningStateRepository {

    @Resource
    private LearningStateMapper learningStateMapper;

    public LearningStateBO selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(learningStateMapper.selectOneByQuery(
                QueryWrapper.create().eq("student_id", studentId).eq("knowledge_id", knowledgeId)), LearningStateBO.class);
    }

    public int upsert(LearningStateBO state) {
        LearningStateBO existing = selectByStudentAndKnowledge(state.getStudentId(), state.getKnowledgeId());
        if (existing != null) {
            state.setId(existing.getId());
            LearningStateDO dataObj = MapstructUtils.convert(state, LearningStateDO.class);
            return learningStateMapper.update(dataObj);
        }
        LearningStateDO dataObj = MapstructUtils.convert(state, LearningStateDO.class);
        return learningStateMapper.insert(dataObj);
    }
}
