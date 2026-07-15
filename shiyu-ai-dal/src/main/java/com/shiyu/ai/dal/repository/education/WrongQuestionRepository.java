package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.WrongQuestionBO;
import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;
import com.shiyu.ai.dal.mapper.education.WrongQuestionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class WrongQuestionRepository {

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    public WrongQuestionBO selectById(Long id) {
        return MapstructUtils.convert(wrongQuestionMapper.selectOneById(id), WrongQuestionBO.class);
    }

    public List<WrongQuestionBO> selectByStudentId(Long studentId) {
        return MapstructUtils.convert(wrongQuestionMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
        ), WrongQuestionBO.class);
    }
    public WrongQuestionBO selectByStudentAndQuestion(Long studentId, Long questionId) {
        return MapstructUtils.convert(wrongQuestionMapper.selectOneByQuery(
                QueryWrapper.create().eq("student_id", studentId).eq("question_id", questionId)), WrongQuestionBO.class);
    }

    public int insert(WrongQuestionBO entity) {
        WrongQuestionDO dataObj = MapstructUtils.convert(entity, WrongQuestionDO.class);
        return wrongQuestionMapper.insert(dataObj);
    }


    public int update(WrongQuestionBO entity) {
        WrongQuestionDO dataObj = MapstructUtils.convert(entity, WrongQuestionDO.class);
        return wrongQuestionMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return wrongQuestionMapper.deleteById(id);
    }

}
