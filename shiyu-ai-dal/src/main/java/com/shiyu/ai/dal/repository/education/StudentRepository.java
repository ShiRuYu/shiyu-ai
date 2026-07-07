package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.dal.mapper.education.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

@Component
public class StudentRepository {

    @Resource
    private StudentMapper studentMapper;

    public StudentDO selectById(Long id) {
        return studentMapper.selectOneById(id);
    }

    public StudentDO selectByUserId(Long userId) {
        return studentMapper.selectOneByQuery(QueryWrapper.create().eq("user_id", userId));
    }

    public PageData<StudentDO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<StudentDO> page = studentMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("status", 1).orderBy("id"));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public List<StudentDO> selectAll() {
        return studentMapper.selectListByQuery(
                QueryWrapper.create().orderBy("grade", true).orderBy("name", true));
    }

    public int insert(StudentDO student) {
        return studentMapper.insert(student);
    }

    public int update(StudentDO student) {
        return studentMapper.update(student);
    }

    public int deleteById(Long id) {
        return studentMapper.deleteById(id);
    }
}
