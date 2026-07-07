package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.dal.repository.education.ReviewTaskRepository;
import com.shiyu.ai.education.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewTaskRepository reviewTaskRepository;

    @Override
    public ReviewTaskDO getById(Long id) {
        return reviewTaskRepository.selectById(id);
    }

    @Override
    public List<ReviewTaskDO> listTodayTasks(Long studentId) {
        return reviewTaskRepository.selectTodayTasks(studentId);
    }

    @Override
    public List<ReviewTaskDO> listByStudentAndStatus(Long studentId, String status) {
        return reviewTaskRepository.selectByStudentAndStatus(studentId, status);
    }

    @Override
    public List<ReviewTaskDO> listByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return reviewTaskRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewTaskDO create(ReviewTaskDO task) {
        reviewTaskRepository.insert(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReviewTaskDO task) {
        reviewTaskRepository.update(task);
    }
}
