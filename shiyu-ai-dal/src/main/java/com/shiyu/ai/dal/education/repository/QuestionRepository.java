package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.QuestionBO;
import com.shiyu.ai.dal.education.dataobject.QuestionDO;
import com.shiyu.ai.dal.education.mapper.QuestionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class QuestionRepository {

    @Resource
    private QuestionMapper questionMapper;

    public QuestionBO selectById(Long id) {
        return MapstructUtils.convert(questionMapper.selectOneById(id), QuestionBO.class);
    }

    public PageData<QuestionBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<QuestionDO> page = questionMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().orderBy("id", false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), QuestionBO.class), page.getTotalRow());
    }

    public List<QuestionBO> selectBySubjectAndGrade(String subjectCode, Integer grade) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("subject_code", subjectCode).eq("grade", grade)), QuestionBO.class);
    }

    public List<QuestionBO> selectByDifficulty(Integer difficulty) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("difficulty", difficulty)), QuestionBO.class);
    }

    public List<QuestionBO> selectByType(String type) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("type", type)), QuestionBO.class);
    }

    public QuestionBO selectByCode(String code) {
        return MapstructUtils.convert(questionMapper.selectOneByQuery(
                QueryWrapper.create().eq("code", code)), QuestionBO.class);
    }

    public void incrementUsedCount(Long id) {
        QuestionDO dataObj = questionMapper.selectOneById(id);
        if (dataObj != null) {
            Long usedCount = dataObj.getUsedCount();
            dataObj.setUsedCount(usedCount != null ? usedCount + 1 : 1L);
            questionMapper.update(dataObj);
        }
    }

    public List<QuestionBO> selectAll() {
        return MapstructUtils.convert(questionMapper.selectListByQuery(QueryWrapper.create()), QuestionBO.class);
    }

    public int insert(QuestionBO entity) {
        QuestionDO dataObj = MapstructUtils.convert(entity, QuestionDO.class);
        return questionMapper.insert(dataObj);
    }

    public int update(QuestionBO entity) {
        QuestionDO dataObj = MapstructUtils.convert(entity, QuestionDO.class);
        return questionMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return questionMapper.deleteById(id);
    }
}
