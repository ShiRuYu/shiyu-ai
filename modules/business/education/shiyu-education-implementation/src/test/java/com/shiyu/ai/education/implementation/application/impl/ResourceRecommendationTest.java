package com.shiyu.ai.education.implementation.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.education.implementation.application.QuestionService;
import com.shiyu.ai.education.implementation.application.ResourceService;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.port.repository.AbilityRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.persistence.dataobject.ResourceKnowledgeDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ResourceKnowledgeMapper;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/** 验证资源推荐按薄弱知识排序，并保持学科、年级和有效学生的访问边界。 */
class ResourceRecommendationTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(31), new UserId(32), false);
    private final AbilityRepository abilities = mock(AbilityRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final ResourceService resources = mock(ResourceService.class);
    private final ResourceKnowledgeMapper links = mock(ResourceKnowledgeMapper.class);
    private final KnowledgePointPort knowledge = mock(KnowledgePointPort.class);
    private final RecommendationServiceImpl service = new RecommendationServiceImpl(
            abilities, mock(QuestionService.class), mock(ReviewService.class), resources, students, links, knowledge);

    @Test
    void ranksEligibleResourcesByWeakestMasteryAndAssociationOrderWithoutDuplicates() {
        when(students.selectById(ACTOR.tenantId(), 7L)).thenReturn(student(1, 7));
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(Arrays.asList(
                null, ability(null, 20D), ability(102L, null), ability(103L, 80D),
                ability(100L, 35D), ability(100L, 20D), ability(101L, 50D)));
        when(knowledge.listStudentEducationPoints(ACTOR)).thenReturn(Arrays.asList(
                null, point(null, "MATH"), point(999L, "MATH"), point(100L, " math "),
                point(101L, "MATH"), point(100L, "MATH")));
        when(resources.listAll(ACTOR)).thenReturn(Arrays.asList(null,
                resource(null, 7, "MATH"), resource(1L, 7, "MATH"), resource(2L, 7, "math"),
                resource(3L, 8, "MATH"), resource(4L, 7, "ENGLISH"), resource(5L, 7, "MATH"),
                resource(6L, 7, null), resource(1L, 7, "MATH")));
        when(links.selectListByQuery(any(QueryWrapper.class))).thenReturn(Arrays.asList(
                null, link(null, 100L, 1), link(1L, null, 1), link(99L, 100L, 1),
                link(1L, 999L, 1), link(3L, 100L, 1), link(4L, 100L, 1), link(6L, 100L, 1),
                link(1L, 100L, null), link(1L, 101L, 1), link(2L, 100L, 1), link(5L, 101L, 1)));

        var result = service.recommendResources(ACTOR, 7L, 10);
        assertEquals(List.of(2L, 1L, 5L), result.stream().map(item -> item.resourceId()).toList());
        assertEquals(List.of(80, 80, 50), result.stream().map(item -> item.score()).toList());
        assertEquals(100L, result.get(1).knowledgeId());
        assertEquals("WEAK_POINT_RESOURCE", result.getFirst().recommendType());
        assertEquals(List.of(2L), service.recommendResources(ACTOR, 7L, 1).stream()
                .map(item -> item.resourceId()).toList());
    }

    @Test
    void doesNotRecommendForInactiveUnclassifiedOrMissingStudents() {
        assertTrue(service.recommendResources(ACTOR, 7L, 0).isEmpty());
        assertTrue(service.recommendResources(ACTOR, null, 2).isEmpty());
        assertTrue(service.recommendResources(null, 7L, 2).isEmpty());
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(students.selectById(ACTOR.tenantId(), 7L)).thenReturn(student(0, 7));
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(students.selectById(ACTOR.tenantId(), 7L)).thenReturn(student(1, null));
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        verifyNoInteractions(resources, knowledge, links);
    }

    @Test
    void emptyOrUnmatchedFeedsNeverProduceUnrelatedRecommendations() {
        when(students.selectById(ACTOR.tenantId(), 7L)).thenReturn(student(1, 7));
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(null);
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(List.of());
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(List.of(ability(100L, 90D)));
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(List.of(ability(100L, 20D)));
        when(knowledge.listStudentEducationPoints(ACTOR)).thenReturn(null);
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(knowledge.listStudentEducationPoints(ACTOR)).thenReturn(List.of());
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(knowledge.listStudentEducationPoints(ACTOR)).thenReturn(List.of(point(100L, " ")));
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(knowledge.listStudentEducationPoints(ACTOR)).thenReturn(List.of(point(100L, "MATH")));
        when(resources.listAll(ACTOR)).thenReturn(null);
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(resources.listAll(ACTOR)).thenReturn(List.of());
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(resources.listAll(ACTOR)).thenReturn(Arrays.asList((ResourceResponse) null));
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(resources.listAll(ACTOR)).thenReturn(List.of(resource(1L, 7, "MATH")));
        when(links.selectListByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
        when(links.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertTrue(service.recommendResources(ACTOR, 7L, 2).isEmpty());
    }

    private static StudentBO student(int status, Integer grade) {
        StudentBO student = new StudentBO();
        student.setStatus(status);
        student.setGrade(grade);
        return student;
    }

    private static AbilityBO ability(Long id, Double mastery) {
        AbilityBO ability = new AbilityBO();
        ability.setKnowledgeId(id);
        ability.setOverallMastery(mastery);
        return ability;
    }

    private static KnowledgeResponse point(Long id, String subject) {
        return new KnowledgeResponse(id, "K", "知识点", null, 1, subject, null, null, null, null);
    }

    private static ResourceResponse resource(Long id, int grade, String subject) {
        return new ResourceResponse(id, "资源", "VIDEO", null, subject, grade, 1, null, null, 0L);
    }

    private static ResourceKnowledgeDO link(Long resourceId, Long knowledgeId, Integer order) {
        ResourceKnowledgeDO link = new ResourceKnowledgeDO();
        link.setResourceId(resourceId);
        link.setKnowledgeId(knowledgeId);
        link.setSortOrder(order);
        return link;
    }
}
