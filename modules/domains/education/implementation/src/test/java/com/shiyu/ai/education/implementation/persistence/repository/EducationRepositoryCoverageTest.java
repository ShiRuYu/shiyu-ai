package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.paginate.Page;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Exercises every tenant-scoped repository operation against a mapper double.
 * The repository SQL/query construction is the subject under test; generated
 * MapStruct implementations are intentionally not part of this test.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class EducationRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(7L);

    private static final List<Class<?>> REPOSITORIES = List.of(
            AbilityRepositoryImpl.class, AchievementRepositoryImpl.class,
            ChapterRepositoryImpl.class, CourseChapterRepositoryImpl.class,
            CourseKnowledgeRepositoryImpl.class, CourseRepositoryImpl.class,
            CourseSectionRepositoryImpl.class, ExamRepositoryImpl.class,
            KnowledgeTextbookRepositoryImpl.class, LearningStateRepositoryImpl.class,
            QuestionRepositoryImpl.class, ResourceRepositoryImpl.class,
            ReviewTaskRepositoryImpl.class, StudentRepositoryImpl.class,
            StudyPlanItemRepositoryImpl.class, StudyPlanRepositoryImpl.class,
            StudyRecordRepositoryImpl.class, SubjectRepositoryImpl.class,
            TextbookRepositoryImpl.class, WrongQuestionRepositoryImpl.class);

    @Test
    void executesAllTenantScopedRepositoryOperations() {
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convert(invocation.getArgument(0), invocation.getArgument(1)));
            conversions.when(() -> MapstructUtils.convert(any(List.class), any(Class.class)))
                    .thenAnswer(invocation -> List.of());
            for (Class<?> repositoryType : REPOSITORIES) {
                assertDoesNotThrow(() -> invokeRepository(repositoryType));
            }
        }
    }

    @Test
    void rejectsStudentWritesWhenTenantScopedMutationAffectsNoRows() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        com.shiyu.ai.education.implementation.persistence.mapper.StudentMapper mapper =
                mock(com.shiyu.ai.education.implementation.persistence.mapper.StudentMapper.class);
        try {
            Field field = StudentRepositoryImpl.class.getDeclaredField("studentMapper");
            field.setAccessible(true);
            field.set(repository, mapper);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        when(mapper.insert(any())).thenReturn(0);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convert(invocation.getArgument(0), invocation.getArgument(1)));
            com.shiyu.ai.education.domain.model.StudentBO student =
                    new com.shiyu.ai.education.domain.model.StudentBO();
            assertThrows(IllegalStateException.class, () -> repository.insert(TENANT, student));
        }
    }

    @Test
    void rejectsLearningStateInsertWhenTenantScopedMutationAffectsNoRows() {
        LearningStateRepositoryImpl repository = new LearningStateRepositoryImpl();
        com.shiyu.ai.education.implementation.persistence.mapper.LearningStateMapper mapper =
                mock(com.shiyu.ai.education.implementation.persistence.mapper.LearningStateMapper.class);
        setMapper(repository, "learningStateMapper", mapper);
        when(mapper.selectOneByQuery(any())).thenReturn(null);
        when(mapper.insert(any())).thenReturn(0);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convert(invocation.getArgument(0), invocation.getArgument(1)));
            assertThrows(IllegalStateException.class,
                    () -> repository.upsert(TENANT, new com.shiyu.ai.education.domain.model.LearningStateBO()));
        }
    }

    @Test
    void rejectsKnowledgeTextbookInsertWhenTenantScopedMutationAffectsNoRows() {
        KnowledgeTextbookRepositoryImpl repository = new KnowledgeTextbookRepositoryImpl();
        com.shiyu.ai.education.implementation.persistence.mapper.KnowledgeTextbookMapper mapper =
                mock(com.shiyu.ai.education.implementation.persistence.mapper.KnowledgeTextbookMapper.class);
        setMapper(repository, "mapper", mapper);
        when(mapper.insert(any())).thenReturn(0);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convert(invocation.getArgument(0), invocation.getArgument(1)));
            assertThrows(IllegalStateException.class,
                    () -> repository.insert(TENANT, new com.shiyu.ai.education.domain.model.KnowledgeTextbookBO()));
        }
    }

    @Test
    void rejectsStudyPlanItemBatchWhenTenantScopedMutationAffectsNoRows() {
        StudyPlanItemRepositoryImpl repository = new StudyPlanItemRepositoryImpl();
        com.shiyu.ai.education.implementation.persistence.mapper.StudyPlanItemMapper mapper =
                mock(com.shiyu.ai.education.implementation.persistence.mapper.StudyPlanItemMapper.class);
        setMapper(repository, "studyPlanItemMapper", mapper);
        when(mapper.insert(any())).thenReturn(0);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convert(invocation.getArgument(0), invocation.getArgument(1)));
            assertThrows(IllegalStateException.class,
                    () -> repository.insertBatch(TENANT,
                            List.of(new com.shiyu.ai.education.domain.model.StudyPlanItemBO())));
        }
    }

    @Test
    void rejectsQuestionUsageUpdateWhenQuestionIsOutsideTenantScope() {
        QuestionRepositoryImpl repository = new QuestionRepositoryImpl();
        com.shiyu.ai.education.implementation.persistence.mapper.QuestionMapper mapper =
                mock(com.shiyu.ai.education.implementation.persistence.mapper.QuestionMapper.class);
        setMapper(repository, "questionMapper", mapper);
        when(mapper.selectOneByQuery(any())).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> repository.incrementUsedCount(TENANT, 1L));
    }

    private static void setMapper(Object repository, String fieldName, Object mapper) {
        try {
            Field field = repository.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(repository, mapper);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void invokeRepository(Class<?> repositoryType) throws Exception {
        Object repository = repositoryType.getDeclaredConstructor().newInstance();
        Field mapperField = findMapperField(repositoryType);
        Class<?> mapperType = mapperField.getType();
        Object mapper = mock(mapperType, mapperAnswer());
        mapperField.setAccessible(true);
        mapperField.set(repository, mapper);
        if (repositoryType == QuestionRepositoryImpl.class) {
            when(((com.shiyu.ai.education.implementation.persistence.mapper.QuestionMapper) mapper)
                    .selectOneByQuery(any())).thenReturn(new com.shiyu.ai.education.implementation.persistence.dataobject.QuestionDO());
        }
        for (Method method : repositoryType.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            Object[] arguments = java.util.Arrays.stream(method.getGenericParameterTypes())
                    .map(EducationRepositoryCoverageTest::argumentFor)
                    .toArray();
            method.setAccessible(true);
            method.invoke(repository, arguments);
        }
    }

    private static Field findMapperField(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().toLowerCase().contains("mapper")) return field;
        }
        throw new IllegalStateException("No mapper field in " + type.getName());
    }

    private static Answer<Object> mapperAnswer() {
        return invocation -> {
            String name = invocation.getMethod().getName();
            if (name.startsWith("selectList") || name.startsWith("selectAll")) return List.of();
            if (name.startsWith("selectOne")) return null;
            if (name.equals("paginate")) return mock(Page.class);
            if (name.startsWith("insert") || name.startsWith("update") || name.startsWith("delete")) return 1;
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        };
    }

    private static Object argumentFor(Type type) {
        if (type == TenantId.class) return TENANT;
        if (type == Long.class || type == long.class) return 1L;
        if (type == Integer.class || type == int.class) return 1;
        if (type == String.class) return "value";
        if (type instanceof ParameterizedType parameterized
                && parameterized.getRawType() == List.class) {
            Type element = parameterized.getActualTypeArguments()[0];
            if (element instanceof Class<?> elementType && elementType != Long.class
                    && elementType != Integer.class && elementType != String.class) {
                Object value = argumentFor(elementType);
                return value == null ? List.of() : List.of(value);
            }
            return List.of(1L);
        }
        if (type instanceof Class<?> clazz && clazz.isEnum()) return clazz.getEnumConstants()[0];
        if (type instanceof Class<?> clazz) {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object convert(Object source, Class<?> targetType) {
        if (source == null) return null;
        try {
            Constructor<?> constructor = targetType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
