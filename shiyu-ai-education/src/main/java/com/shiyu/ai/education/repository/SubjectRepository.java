package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import com.shiyu.ai.dal.mapper.education.SubjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubjectRepository {

    @Resource
    private SubjectMapper subjectMapper;

    public SubjectDO selectById(Long id) {
        return subjectMapper.selectOneById(id);
    }

    public SubjectDO selectByCode(String code) {
        return subjectMapper.selectOneByQuery(QueryWrapper.create().eq("code", code));
    }

    public List<SubjectDO> selectAll() {
        return subjectMapper.selectListByQuery(QueryWrapper.create().eq("status", 1).orderBy("sort_order"));
    }

    public List<SubjectDO> selectByGradeLevel(String gradeLevel) {
        return subjectMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = 1")
                        .and("grade_level = ? OR grade_level = 'ALL'", gradeLevel)
                        .orderBy("sort_order"));
    }

    public int insert(SubjectDO subject) {
        return subjectMapper.insert(subject);
    }

    public int update(SubjectDO subject) {
        return subjectMapper.update(subject);
    }

    public int deleteById(Long id) {
        return subjectMapper.deleteById(id);
    }
}
