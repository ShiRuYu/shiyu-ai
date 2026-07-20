package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.CourseChapterBO;
import com.shiyu.ai.dal.education.dataobject.CourseChapterDO;
import com.shiyu.ai.dal.education.mapper.CourseChapterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseChapterRepository {

    @Resource
    private CourseChapterMapper courseChapterMapper;

    public List<CourseChapterBO> selectByCourseId(Long courseId) {
        return MapstructUtils.convert(courseChapterMapper.selectListByQuery(
                QueryWrapper.create().eq("course_id", courseId).orderBy("order_no", true)), CourseChapterBO.class);
    }
}
