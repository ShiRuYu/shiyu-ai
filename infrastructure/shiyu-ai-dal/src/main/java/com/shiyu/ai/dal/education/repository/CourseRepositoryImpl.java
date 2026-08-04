package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseBO;
import com.shiyu.ai.dal.education.dataobject.CourseDO;
import com.shiyu.ai.dal.education.mapper.CourseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseRepositoryImpl implements com.shiyu.ai.education.port.repository.CourseRepository {

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
        int rows = courseMapper.insert(dataObj);
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(CourseBO entity) {
        CourseDO dataObj = MapstructUtils.convert(entity, CourseDO.class);
        return courseMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return courseMapper.deleteById(id);
    }

}
