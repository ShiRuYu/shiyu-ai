package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.StudyRecordBO;
import com.shiyu.ai.dal.education.dataobject.StudyRecordDO;
import com.shiyu.ai.dal.education.mapper.StudyRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudyRecordRepository {

    @Resource
    private StudyRecordMapper studyRecordMapper;

    public List<StudyRecordBO> selectByStudent(Long studentId) {
        return MapstructUtils.convert(studyRecordMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .orderBy("create_time", false)
        ), StudyRecordBO.class);
    }

    public List<StudyRecordBO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(studyRecordMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("create_time", false)
        ), StudyRecordBO.class);
    }

    public int insert(StudyRecordBO record) {
        StudyRecordDO dataObj = MapstructUtils.convert(record, StudyRecordDO.class);
        int rows = studyRecordMapper.insert(dataObj);
        record.setId(dataObj.getId());
        return rows;
    }

}
