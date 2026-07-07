package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;
import com.shiyu.ai.dal.mapper.education.WrongQuestionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WrongQuestionRepository {

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    public WrongQuestionDO selectById(Long id) {
        return wrongQuestionMapper.selectOneById(id);
    }

    public List<WrongQuestionDO> selectByStudentId(Long studentId) {
        return wrongQuestionMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).orderBy("created_at", false));
    }

    public WrongQuestionDO selectByStudentAndQuestion(Long studentId, Long questionId) {
        return wrongQuestionMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("question_id", questionId));
    }

    public int insert(WrongQuestionDO wrongQuestion) {
        return wrongQuestionMapper.insert(wrongQuestion);
    }

    public int update(WrongQuestionDO wrongQuestion) {
        return wrongQuestionMapper.update(wrongQuestion);
    }

    public int deleteById(Long id) {
        return wrongQuestionMapper.deleteById(id);
    }
}
