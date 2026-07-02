package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.CourseChapterDO;
import com.shiyu.ai.dal.mapper.education.CourseChapterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseChapterRepository {

    @Resource
    private CourseChapterMapper courseChapterMapper;

    public List<CourseChapterDO> selectByCourseId(Long courseId) {
        return courseChapterMapper.selectListByQuery(
                QueryWrapper.create().eq("course_id", courseId).orderBy("order_no", true));
    }
}
