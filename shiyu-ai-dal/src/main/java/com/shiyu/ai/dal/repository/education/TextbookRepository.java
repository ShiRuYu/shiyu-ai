package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.TextbookDO;
import com.shiyu.ai.dal.mapper.education.TextbookMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

@Component
public class TextbookRepository {

    @Resource
    private TextbookMapper textbookMapper;

    public TextbookDO selectById(Long id) {
        return textbookMapper.selectOneById(id);
    }

    public List<TextbookDO> selectBySubjectAndGrade(String subjectCode, Integer grade) {
        return textbookMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
                        .eq("grade", grade));
    }

    public PageData<TextbookDO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<TextbookDO> page = textbookMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("status", 1).orderBy("id"));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public List<TextbookDO> selectAll() {
        return textbookMapper.selectAll();
    }

    public int insert(TextbookDO textbook) {
        return textbookMapper.insert(textbook);
    }

    public int update(TextbookDO textbook) {
        return textbookMapper.update(textbook);
    }

    public int deleteById(Long id) {
        return textbookMapper.deleteById(id);
    }
}
