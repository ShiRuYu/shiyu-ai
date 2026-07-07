package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.dal.mapper.education.ReviewTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReviewTaskRepository {

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    public ReviewTaskDO selectById(Long id) {
        return reviewTaskMapper.selectOneById(id);
    }

    public List<ReviewTaskDO> selectTodayTasks(Long studentId) {
        return reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("review_date", LocalDate.now())
                        .orderBy("review_round"));
    }

    public List<ReviewTaskDO> selectByStudentAndStatus(Long studentId, String status) {
        return reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("status", status)
                        .orderBy("review_date"));
    }

    public List<ReviewTaskDO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("review_round"));
    }

    public int insert(ReviewTaskDO task) {
        return reviewTaskMapper.insert(task);
    }

    public int update(ReviewTaskDO task) {
        return reviewTaskMapper.update(task);
    }
}
