package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.implementation.web.dto.StudentTutorResponse;
import com.shiyu.ai.education.implementation.web.request.StudentTutorRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 将学生辅导限定为四种教育操作，并仅使用已登录学生本人的数据。 */
@Service
@RequiredArgsConstructor
public class StudentTutorService {
    private static final Set<String> STYLES = Set.of("textual", "visual", "interactive");
    private final StudentRepository students;
    private final StudyRecordRepository records;
    private final KnowledgePointPort points;
    private final ChatEngine chatEngine;

    /** 列出明确发布给当前操作者所属租户的知识点。 */
    public List<KnowledgeResponse> knowledge(ActorContext actor) {
        requireStudent(actor);
        return points.listStudentEducationPoints(actor);
    }

    /** 根据所选且对当前租户可见的知识点生成讲解。 */
    public StudentTutorResponse teach(ActorContext actor, StudentTutorRequest request) {
        requireStudent(actor);
        KnowledgeResponse point = point(actor, request);
        String style = request.style() == null ? "textual" : request.style();
        if (!STYLES.contains(style)) throw new ServiceException("讲解风格无效");
        String prompt = "你是学生辅学讲解老师。仅依据以下知识点讲解，不编造来源。\n知识点："
                + point.name() + "\n内容：" + description(point)
                + "\n风格：" + style + "\n请用中文解释并给出一个简短例子。";
        return result(point, generate(actor, prompt));
    }

    /** 根据所选知识点生成练习建议。 */
    public StudentTutorResponse practice(ActorContext actor, StudentTutorRequest request) {
        requireStudent(actor);
        KnowledgeResponse point = point(actor, request);
        int count = request.count() == null ? 3 : request.count();
        int difficulty = request.difficulty() == null ? 2 : request.difficulty();
        if (count < 1 || count > 10 || difficulty < 1 || difficulty > 4) {
            throw new ServiceException("出题参数无效");
        }
        String prompt = "你是学生辅学出题老师。仅依据以下知识点生成" + count
                + "道练习题，难度等级" + difficulty + "。标注这些题是 AI 生成的练习建议，不进入正式题库或成绩。"
                + "\n知识点：" + point.name() + "\n内容：" + description(point);
        return result(point, generate(actor, prompt));
    }

    /** 针对一个可见知识点和已校验的未来日期生成学习计划。 */
    public StudentTutorResponse plan(ActorContext actor, StudentTutorRequest request) {
        requireStudent(actor);
        KnowledgeResponse point = point(actor, request);
        LocalDate target;
        try { target = LocalDate.parse(request.targetDate()); }
        catch (Exception error) { throw new ServiceException("目标日期无效"); }
        if (!target.isAfter(LocalDate.now()) || target.isAfter(LocalDate.now().plusYears(1))) {
            throw new ServiceException("目标日期无效");
        }
        String prompt = "你是学生学习规划师。请为下列知识点制定截至" + target
                + "的具体学习步骤。没有学习记录时不要断言学生的掌握程度。\n知识点："
                + point.name() + "\n内容：" + description(point);
        return result(point, generate(actor, prompt));
    }

    /** 仅汇总已登录学生在指定时间段内的学习记录。 */
    public StudentTutorResponse report(ActorContext actor, StudentTutorRequest request) {
        StudentBO student = requireStudent(actor);
        String period = request == null ? null : request.period();
        int days = switch (period == null ? "30d" : period.toLowerCase()) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            default -> throw new ServiceException("报告周期无效");
        };
        LocalDateTime start = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        List<StudyRecordBO> matching = records.selectByStudent(actor.tenantId(), student.getId())
                .stream().filter(record -> record.getCreateTime() != null
                        && !record.getCreateTime().isBefore(start)).toList();
        String label = days + "d";
        if (matching.isEmpty()) {
            return new StudentTutorResponse("当前周期没有学习记录，先完成练习后再查看报告。",
                    null, null, label, 0, null, true, List.of());
        }
        Double average = matching.stream().filter(record -> record.getScore() != null)
                .mapToDouble(StudyRecordBO::getScore).average().stream().boxed().findFirst()
                .orElse(null);
        long scored = matching.stream().filter(record -> record.getScore() != null).count();
        String prompt = "你是学习报告助手。只能根据以下当前学生在" + label
                + "内的服务端学习记录统计生成报告。没有证据时不要判断掌握度或进步。"
                + "\n学习次数：" + matching.size() + "\n有分数记录：" + scored
                + "\n平均分：" + (average == null ? "无数据" : average)
                + "\n请给出简短、可操作的下一步建议。";
        List<StudentTutorResponse.SourceRecord> sources = matching.stream()
                .map(record -> new StudentTutorResponse.SourceRecord(record.getId(),
                        record.getRecordType(), record.getQuestionId(), record.getScore(),
                        record.getCreateTime()))
                .toList();
        return new StudentTutorResponse(generate(actor, prompt), null, null, label,
                matching.size(), average, false, sources);
    }

    private KnowledgeResponse point(ActorContext actor, StudentTutorRequest request) {
        if (request == null || request.knowledgeId() == null) {
            throw new ServiceException("请选择知识点");
        }
        return points.getStudentEducationPoint(actor, request.knowledgeId());
    }

    private StudentBO requireStudent(ActorContext actor) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null) {
            throw new ServiceException("无法识别当前学生");
        }
        StudentBO student = students.selectByUserId(actor.tenantId(), actor.userId().value());
        if (student == null || !Integer.valueOf(1).equals(student.getStatus())) {
            throw new ServiceException("当前账号未关联可用的学生档案");
        }
        return student;
    }

    private String generate(ActorContext actor, String prompt) {
        ChatResponse response = chatEngine.chat(ChatRequest.builder()
                .platform("default")
                .tenantId(actor.tenantId().value()).userId(actor.userId().value())
                .messages(List.of(ChatMessage.text("user", prompt))).build());
        if (response == null || !response.isSuccess() || response.getContent() == null
                || response.getContent().isBlank()) throw new ServiceException("辅学生成失败，请稍后重试");
        return response.getContent();
    }

    private static StudentTutorResponse result(KnowledgeResponse point, String content) {
        return new StudentTutorResponse(content, point.id(), point.name(), null, 0, null,
                false, List.of());
    }

    private static String description(KnowledgeResponse point) {
        if (point.description() == null || point.description().isBlank()) {
            throw new ServiceException("知识点内容不足，暂不能生成辅学内容");
        }
        return point.description();
    }
}
