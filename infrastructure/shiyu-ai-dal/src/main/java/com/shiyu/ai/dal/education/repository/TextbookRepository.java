package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.TextbookBO;
import com.shiyu.ai.dal.education.dataobject.TextbookDO;
import com.shiyu.ai.dal.education.mapper.TextbookMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class TextbookRepository implements com.shiyu.ai.education.port.repository.TextbookRepository {

    @Resource
    private TextbookMapper textbookMapper;

    public TextbookBO selectById(Long id) {
        return MapstructUtils.convert(textbookMapper.selectOneById(id), TextbookBO.class);
    }

    public PageData<TextbookBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<TextbookDO> page = textbookMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), TextbookBO.class), page.getTotalRow());
    }

    public List<TextbookBO> selectBySubjectAndGrade(String subjectCode, Integer grade) {
        return MapstructUtils.convert(textbookMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
                        .eq("grade", grade)
        ), TextbookBO.class);
    }
    public List<TextbookBO> selectAll() {
        return MapstructUtils.convert(textbookMapper.selectListByQuery(QueryWrapper.create()), TextbookBO.class);
    }

    public int insert(TextbookBO entity) {
        TextbookDO dataObj = MapstructUtils.convert(entity, TextbookDO.class);
        int rows = textbookMapper.insert(dataObj);
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(TextbookBO entity) {
        TextbookDO dataObj = MapstructUtils.convert(entity, TextbookDO.class);
        return textbookMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return textbookMapper.deleteById(id);
    }

}
