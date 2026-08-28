package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.paginate.Page;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.*;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.*;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked", "try"})
class KnowledgeRelationAndDocumentRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(31L);

    @Test
    void exercisesDocumentRepositoryQueriesMutationsAndTenantGuards() throws Exception {
        KnowledgeDocumentMapper documents = mock(KnowledgeDocumentMapper.class, mapperAnswer(KnowledgeDocumentDO.class));
        KnowledgeDocRelationMapper relations = mock(KnowledgeDocRelationMapper.class, mapperAnswer(KnowledgeDocRelationDO.class));
        KnowledgeDocumentRepositoryImpl repository = new KnowledgeDocumentRepositoryImpl();
        inject(repository, "knowledgeDocumentMapper", documents);
        inject(repository, "knowledgeDocRelationMapper", relations);
        when(relations.selectListByQuery(any())).thenReturn(List.of(relation(8L, 9L)));

        try (MockedStatic<MapstructUtils> conversions = conversionMock()) {
            assertNotNull(repository.selectById(TENANT, 1L));
            assertEquals(1, repository.selectAll(TENANT).size());
            KnowledgeDocumentBO document = new KnowledgeDocumentBO();
            assertEquals(1, repository.insert(TENANT, document));
            document.setTenantId(TENANT.value()); document.setId(1L);
            assertEquals(1, repository.update(TENANT, document));
            when(documents.selectOneByQuery(any())).thenReturn(null);
            assertEquals(0, repository.update(TENANT, document));
            when(documents.selectOneByQuery(any())).thenAnswer(invocation -> {
                KnowledgeDocumentDO value = new KnowledgeDocumentDO();
                value.setId(1L); value.setSpaceId(2L);
                return value;
            });
            assertEquals(1, repository.deleteById(TENANT, 1L));
            assertEquals(1, repository.searchByKeyword(TENANT, "math", 5).size());
            assertEquals(1, repository.selectByKnowledgeId(TENANT, 9L).size());
            assertEquals(1, repository.selectByKnowledgeId(TENANT, 2L, 9L).size());
            when(relations.selectListByQuery(any())).thenReturn(List.of());
            assertEquals(0, repository.selectByKnowledgeId(TENANT, 2L, 10L).size());
            assertEquals(1, repository.pageBySpace(TENANT, 2L, 1, 10, null, null, null).getTotal());
            assertEquals(1, repository.pageBySpace(TENANT, 2L, 1, 10, "math", "PUBLISHED", "READY").getTotal());
            assertNotNull(repository.findBySpaceAndChecksum(TENANT, 2L, "sha"));
            assertEquals(1, repository.findBySpace(TENANT, 2L).size());
            repository.assignDefaultSpace(TENANT, 2L);
        }
        assertThrows(IllegalArgumentException.class, () -> repository.selectById(null, 1L));
        KnowledgeDocumentBO wrong = new KnowledgeDocumentBO(); wrong.setTenantId(999L); wrong.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> repository.update(TENANT, wrong));
    }

    @Test
    void exercisesChunkRelationAndDocumentRelationRepositories() throws Exception {
        KnowledgeChunkMapper chunks = mock(KnowledgeChunkMapper.class, mapperAnswer(KnowledgeChunkDO.class));
        KnowledgeChunkRepositoryImpl chunkRepository = new KnowledgeChunkRepositoryImpl(chunks);
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        KnowledgeChunkDO chunkData = new KnowledgeChunkDO(); chunkData.setId(4L); chunkData.setSpaceId(null);
        when(chunks.selectOneByQuery(any())).thenReturn(chunkData);
        when(chunks.selectListByQuery(any())).thenReturn(List.of(chunkData));
        try (MockedStatic<MapstructUtils> conversions = conversionMock()) {
            chunkRepository.insert(TENANT, chunk);
            assertNotNull(chunkRepository.getById(TENANT, 4L));
            assertEquals(1, chunkRepository.findBySpace(TENANT, 2L).size());
            chunkRepository.deleteByDocumentId(TENANT, 8L);
            chunkRepository.assignDefaultSpace(TENANT, 2L);
        }
        assertThrows(IllegalArgumentException.class, () -> chunkRepository.getById(null, 1L));

        KnowledgeRelationMapper relationMapper = mock(KnowledgeRelationMapper.class, mapperAnswer(KnowledgeRelationDO.class));
        KnowledgeRelationRepositoryImpl relationRepository = new KnowledgeRelationRepositoryImpl();
        inject(relationRepository, "relationMapper", relationMapper);
        KnowledgeRelationBO relation = new KnowledgeRelationBO();
        try (MockedStatic<MapstructUtils> conversions = conversionMock()) {
            assertEquals(1, relationRepository.findBySourceId(TENANT, 2L, 1L).size());
            assertEquals(1, relationRepository.findByTargetId(TENANT, 2L, 1L).size());
            assertEquals(1, relationRepository.findBySourceIdAndType(TENANT, 2L, 1L, "RELATED").size());
            assertEquals(1, relationRepository.findByTargetIdAndType(TENANT, 2L, 1L, "RELATED").size());
            assertEquals(1, relationRepository.insert(TENANT, relation));
            assertEquals(1, relationRepository.deleteBySourceAndTargetAndType(TENANT, 2L, 1L, 2L, "RELATED"));
            assertEquals(2, relationRepository.deleteBySourceIdOrTargetId(TENANT, 2L, 1L));
            assertEquals(1, relationRepository.findBySpace(TENANT, 2L).size());
            assertTrue(relationRepository.exists(TENANT, 2L, 1L, 2L, "RELATED"));
            relationRepository.assignDefaultSpace(TENANT, 2L);
        }

        KnowledgeDocRelationMapper docRelationMapper = mock(KnowledgeDocRelationMapper.class, mapperAnswer(KnowledgeDocRelationDO.class));
        KnowledgeDocRelationRepositoryImpl docRelationRepository = new KnowledgeDocRelationRepositoryImpl();
        inject(docRelationRepository, "mapper", docRelationMapper);
        try (MockedStatic<MapstructUtils> conversions = conversionMock()) {
            docRelationRepository.insertBatch(TENANT, List.of(new KnowledgeDocRelationBO()));
            docRelationRepository.deleteByKnowledgeId(TENANT, 2L, 1L);
            assertEquals(1, docRelationRepository.selectByDocId(TENANT, 2L, 1L).size());
            assertEquals(1, docRelationRepository.selectByKnowledgeId(TENANT, 2L, 1L).size());
            docRelationRepository.deleteByDocId(TENANT, 2L, 1L);
            docRelationRepository.assignDefaultSpace(TENANT, 2L);
        }
        assertThrows(IllegalArgumentException.class, () -> relationRepository.findBySpace(null, 2L));
    }

    private static KnowledgeDocRelationDO relation(Long documentId, Long knowledgeId) {
        KnowledgeDocRelationDO value = new KnowledgeDocRelationDO();
        value.setDocId(documentId); value.setKnowledgeId(knowledgeId); value.setSpaceId(2L);
        return value;
    }

    private static Answer<Object> mapperAnswer(Class<?> dataType) {
        return invocation -> {
            String name = invocation.getMethod().getName();
            if (name.startsWith("selectCount")) return 1L;
            if (name.startsWith("selectList")) return List.of(newValue(dataType));
            if (name.startsWith("selectOne")) return newValue(dataType);
            if (name.equals("paginate")) {
                Page<Object> page = Page.of(1, 10, 1);
                page.setRecords(List.of(newValue(dataType)));
                return page;
            }
            if (name.startsWith("insert") || name.startsWith("update") || name.startsWith("delete")) return 1;
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        };
    }

    private static Object newValue(Class<?> type) throws Exception {
        Object value = type.getDeclaredConstructor().newInstance();
        try { type.getMethod("setId", Long.class).invoke(value, 1L); } catch (NoSuchMethodException ignored) { }
        return value;
    }

    private static MockedStatic<MapstructUtils> conversionMock() {
        MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class);
        conversions.when(() -> MapstructUtils.convert(any(List.class), any(Class.class)))
                .thenAnswer(invocation -> convertList((List<?>) invocation.getArgument(0), invocation.getArgument(1)));
        conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                .thenAnswer(invocation -> convertValue(invocation.getArgument(0), invocation.getArgument(1)));
        return conversions;
    }

    private static Object convertValue(Object source, Class<?> type) throws Exception {
        if (source == null) return null;
        Object value = newValue(type);
        if (value instanceof KnowledgeDocumentBO document) {
            document.setId(1L); document.setSpaceId(2L); document.setTitle("math"); document.setContent("math content");
        }
        return value;
    }

    private static List<?> convertList(List<?> source, Class<?> type) throws Exception {
        if (source == null) return List.of();
        List<Object> result = new ArrayList<>();
        for (Object ignored : source) result.add(convertValue(ignored, type));
        return result;
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
