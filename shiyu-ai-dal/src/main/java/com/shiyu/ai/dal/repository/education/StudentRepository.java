package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.StudentBO;
import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.dal.mapper.education.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudentRepository {

    @Resource
    private StudentMapper studentMapper;

    public StudentBO selectById(Long id) {
        return MapstructUtils.convert(studentMapper.selectOneById(id), StudentBO.class);
    }

    public StudentBO selectByUserId(Long userId) {
        return MapstructUtils.convert(studentMapper.selectOneById(userId), StudentBO.class);
    }

    public PageData<StudentBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<StudentDO> page = studentMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), StudentBO.class), page.getTotalRow());
    }

    public List<StudentBO> selectAll() {
        return MapstructUtils.convert(studentMapper.selectListByQuery(QueryWrapper.create()), StudentBO.class);
    }
    public int insert(StudentBO entity) {
        StudentDO dataObj = MapstructUtils.convert(entity, StudentDO.class);
        return studentMapper.insert(dataObj);
    }


    public int update(StudentBO entity) {
        StudentDO dataObj = MapstructUtils.convert(entity, StudentDO.class);
        return studentMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return studentMapper.deleteById(id);
    }

}
