package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.dal.mapper.education.QuestionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

@Component
public class QuestionRepository {

    @Resource
    private QuestionMapper questionMapper;

    public QuestionDO selectById(Long id) {
        return questionMapper.selectOneById(id);
    }

    public QuestionDO selectByCode(String code) {
        return questionMapper.selectOneByQuery(QueryWrapper.create().eq("code", code));
    }

    public List<QuestionDO> selectBySubjectAndGrade(String subjectCode, Integer grade) {
        return questionMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
                        .eq("grade", grade)
                        .eq("status", 1));
    }


    public PageData<QuestionDO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<QuestionDO> page = questionMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("status", 1).orderBy("id"));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public List<QuestionDO> selectAll() {
        return questionMapper.selectListByQuery(
                QueryWrapper.create().eq("status", 1));
    }

    public List<QuestionDO> selectByDifficulty(Integer difficulty) {
        return questionMapper.selectListByQuery(
                QueryWrapper.create().eq("difficulty", difficulty).eq("status", 1));
    }

    public List<QuestionDO> selectByType(String type) {
        return questionMapper.selectListByQuery(
                QueryWrapper.create().eq("type", type).eq("status", 1));
    }

    public int insert(QuestionDO question) {
        return questionMapper.insert(question);
    }

    public int update(QuestionDO question) {
        return questionMapper.update(question);
    }

    public int deleteById(Long id) {
        return questionMapper.deleteById(id);
    }

    public int incrementUsedCount(Long id) {
        QuestionDO q = questionMapper.selectOneById(id);
        if (q != null) {
            q.setUsedCount(q.getUsedCount() + 1);
            return questionMapper.update(q);
        }
        return 0;
    }
}
