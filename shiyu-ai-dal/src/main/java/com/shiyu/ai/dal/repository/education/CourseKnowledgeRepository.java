package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.CourseKnowledgeBO;
import com.shiyu.ai.dal.dataobject.education.CourseKnowledgeDO;
import com.shiyu.ai.dal.mapper.education.CourseKnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseKnowledgeRepository {

    @Resource
    private CourseKnowledgeMapper courseKnowledgeMapper;

    public List<CourseKnowledgeBO> selectByCourseId(Long courseId) {
        return MapstructUtils.convert(courseKnowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq("course_id", courseId)), CourseKnowledgeBO.class);
    }
}
