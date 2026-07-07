package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.dal.dataobject.education.CourseKnowledgeDO;
import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.education.service.CourseService;
import com.shiyu.ai.education.dto.CourseProgressResponse;
import com.shiyu.ai.dal.repository.education.CourseKnowledgeRepository;
import com.shiyu.ai.dal.repository.education.CourseRepository;
import com.shiyu.ai.dal.repository.education.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseKnowledgeRepository courseKnowledgeRepository;
    private final StudyRecordRepository studyRecordRepository;

    @Override
    public CourseDO getById(Long id) {
        return courseRepository.selectById(id);
    }

    @Override
    public List<CourseDO> listBySubjectCode(String subjectCode) {
        return courseRepository.selectBySubjectCode(subjectCode);
    }

    @Override
    public List<CourseDO> listByGrade(Integer grade) {
        return courseRepository.selectByGrade(grade);
    }

    @Override
    public PageData<CourseDO> page(int pageNum, int pageSize) {
        return courseRepository.selectPage(pageNum, pageSize);
    }

    public List<CourseDO> listAll() {
        return courseRepository.selectAll();
    }

    @Override
    public CourseDO create(CourseDO course) {
        courseRepository.insert(course);
        return course;
    }

    @Override
    public void update(CourseDO course) {
        courseRepository.update(course);
    }

    @Override
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public CourseProgressResponse getProgress(Long courseId, Long studentId) {
        CourseDO course = courseRepository.selectById(courseId);
        if (course == null) return null;

        // 获取课程关联的知识点
        List<CourseKnowledgeDO> ckList = courseKnowledgeRepository.selectByCourseId(courseId);
        if (ckList.isEmpty()) {
            return new CourseProgressResponse(courseId, course.getName(), 0, 0, 0.0);
        }

        int totalKnowledges = ckList.size();
        // 统计学生已完成学习的知识点
        long completedKnowledges = 0;
        for (CourseKnowledgeDO ck : ckList) {
            List<StudyRecordDO> records = studyRecordRepository.selectByStudentAndKnowledge(
                    studentId, ck.getKnowledgeId());
            if (!records.isEmpty()) {
                completedKnowledges++;
            }
        }

        double progress = totalKnowledges > 0
                ? Math.round((double) completedKnowledges / totalKnowledges * 100 * 10.0) / 10.0
                : 0.0;

        log.info("课程进度: courseId={}, studentId={}, {}/{}={}%",
                courseId, studentId, completedKnowledges, totalKnowledges, progress);

        return new CourseProgressResponse(courseId, course.getName(),
                (int) completedKnowledges, totalKnowledges, progress);
    }
}
