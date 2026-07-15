package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.ExamBO;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.dal.mapper.education.ExamMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExamRepository {

    @Resource
    private ExamMapper examMapper;

    public ExamBO selectById(Long id) {
        return MapstructUtils.convert(examMapper.selectOneById(id), ExamBO.class);
    }

    public PageData<ExamBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<ExamDO> page = examMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), ExamBO.class), page.getTotalRow());
    }

    public List<ExamBO> selectBySubjectCode(String subjectCode) {
        return MapstructUtils.convert(examMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
        ), ExamBO.class);
    }

    public List<ExamBO> selectByTeacherId(Long teacherId) {
        return MapstructUtils.convert(examMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("teacher_id", teacherId)
        ), ExamBO.class);
    }
    public List<ExamBO> selectAll() {
        return MapstructUtils.convert(examMapper.selectListByQuery(QueryWrapper.create()), ExamBO.class);
    }

    public int insert(ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        return examMapper.insert(dataObj);
    }


    public int update(ExamBO entity) {
        ExamDO dataObj = MapstructUtils.convert(entity, ExamDO.class);
        return examMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return examMapper.deleteById(id);
    }

}
