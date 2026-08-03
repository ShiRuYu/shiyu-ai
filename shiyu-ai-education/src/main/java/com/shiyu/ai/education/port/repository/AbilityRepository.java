package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.AbilityBO;
import java.util.List;

public interface AbilityRepository {
    AbilityBO selectByStudentAndKnowledge(Long studentId, Long knowledgeId);
    List<AbilityBO> selectByStudent(Long studentId);
    int insert(AbilityBO ability);
    int update(AbilityBO ability);
}
