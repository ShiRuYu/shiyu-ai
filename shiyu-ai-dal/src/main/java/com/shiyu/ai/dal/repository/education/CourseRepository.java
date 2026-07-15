package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.CourseBO;
import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.dal.mapper.education.CourseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseRepository {

    @Resource
    private CourseMapper courseMapper;

    public CourseBO selectById(Long id) {
        return MapstructUtils.convert(courseMapper.selectOneById(id), CourseBO.class);
    }

    public PageData<CourseBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<CourseDO> page = courseMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), CourseBO.class), page.getTotalRow());
    }

    public List<CourseBO> selectBySubjectCode(String subjectCode) {
        return MapstructUtils.convert(courseMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
        ), CourseBO.class);
    }

    public List<CourseBO> selectByGrade(Integer grade) {
        return MapstructUtils.convert(courseMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("grade", grade)
        ), CourseBO.class);
    }
    public List<CourseBO> selectAll() {
        return MapstructUtils.convert(courseMapper.selectListByQuery(QueryWrapper.create()), CourseBO.class);
    }

    public int insert(CourseBO entity) {
        CourseDO dataObj = MapstructUtils.convert(entity, CourseDO.class);
        return courseMapper.insert(dataObj);
    }


    public int update(CourseBO entity) {
        CourseDO dataObj = MapstructUtils.convert(entity, CourseDO.class);
        return courseMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return courseMapper.deleteById(id);
    }

}
