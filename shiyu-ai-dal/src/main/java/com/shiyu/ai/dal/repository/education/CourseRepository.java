package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.dal.mapper.education.CourseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

@Component
public class CourseRepository {

    @Resource
    private CourseMapper courseMapper;

    public CourseDO selectById(Long id) {
        return courseMapper.selectOneById(id);
    }

    public List<CourseDO> selectBySubjectCode(String subjectCode) {
        return courseMapper.selectListByQuery(
                QueryWrapper.create().eq("subject_code", subjectCode).eq("status", 1));
    }

    public List<CourseDO> selectByGrade(Integer grade) {
        return courseMapper.selectListByQuery(
                QueryWrapper.create().eq("grade", grade).eq("status", 1));
    }

    public PageData<CourseDO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<CourseDO> page = courseMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("status", 1).orderBy("id"));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public List<CourseDO> selectAll() {
        return courseMapper.selectListByQuery(QueryWrapper.create().eq("status", 1));
    }

    public int insert(CourseDO course) {
        return courseMapper.insert(course);
    }

    public int update(CourseDO course) {
        return courseMapper.update(course);
    }

    public int deleteById(Long id) {
        return courseMapper.deleteById(id);
    }
}
