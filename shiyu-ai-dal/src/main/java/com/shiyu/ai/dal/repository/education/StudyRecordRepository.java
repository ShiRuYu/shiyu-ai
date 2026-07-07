package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.dal.mapper.education.StudyRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyRecordRepository {

    @Resource
    private StudyRecordMapper studyRecordMapper;

    public StudyRecordDO selectById(Long id) {
        return studyRecordMapper.selectOneById(id);
    }

    public List<StudyRecordDO> selectByStudentId(Long studentId) {
        return studyRecordMapper.selectListByQuery(
                QueryWrapper.create().eq("student_id", studentId).orderBy("create_time", false));
    }

    public List<StudyRecordDO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return studyRecordMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("create_time", false));
    }

    public int insert(StudyRecordDO record) {
        return studyRecordMapper.insert(record);
    }
}
