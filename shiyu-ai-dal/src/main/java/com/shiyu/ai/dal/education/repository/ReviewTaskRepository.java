package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDate;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.ReviewTaskBO;
import com.shiyu.ai.dal.education.dataobject.ReviewTaskDO;
import com.shiyu.ai.dal.education.mapper.ReviewTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReviewTaskRepository {

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    public ReviewTaskBO selectById(Long id) {
        return MapstructUtils.convert(reviewTaskMapper.selectOneById(id), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectTodayTasks(Long studentId) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("review_date", LocalDate.now())
                        .orderBy("review_round")
        ), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectByStudentAndStatus(Long studentId, String status) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("status", status)
                        .orderBy("review_date")
        ), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("review_round")
        ), ReviewTaskBO.class);
    }

    public int insert(ReviewTaskBO entity) {
        ReviewTaskDO dataObj = MapstructUtils.convert(entity, ReviewTaskDO.class);
        return reviewTaskMapper.insert(dataObj);
    }

    public int update(ReviewTaskBO entity) {
        ReviewTaskDO dataObj = MapstructUtils.convert(entity, ReviewTaskDO.class);
        return reviewTaskMapper.update(dataObj);
    }

}
