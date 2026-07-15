package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.CourseChapterBO;
import com.shiyu.ai.dal.dataobject.education.CourseChapterDO;
import com.shiyu.ai.dal.mapper.education.CourseChapterMapper;
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
