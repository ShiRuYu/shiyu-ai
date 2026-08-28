package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.domain.model.RecordBO;
import com.shiyu.ai.record.domain.model.TagBO;
import com.shiyu.ai.record.domain.model.TimelineEventBO;
import com.shiyu.ai.record.domain.model.MediaBO;
import com.shiyu.ai.record.domain.model.ProfileBO;
import com.shiyu.ai.record.implementation.persistence.dataobject.RecordDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.TagDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.TimelineEventDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.MediaDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.ProfileDO;
import com.shiyu.ai.record.implementation.persistence.mapper.MediaMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.ProfileMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.RecordMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.TagMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.TimelineEventMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@SuppressWarnings({"rawtypes", "unchecked"})
class RecordRepositoriesTenantTest {
    private static final TenantId TENANT = new TenantId(17);

    @Test
    void exercisesTagAndRecordRepositoriesWithTenantScopedQueries() throws Exception {
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(TagBO.class))).thenReturn(List.of(new TagBO()));
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(RecordBO.class))).thenReturn(List.of(new RecordBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(TagBO.class))).thenReturn(new TagBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(RecordBO.class))).thenReturn(new RecordBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(TagDO.class))).thenReturn(new TagDO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(RecordDO.class))).thenReturn(new RecordDO());
        TagMapper tagMapper = mock(TagMapper.class);
        when(tagMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(2L);
        when(tagMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new TagDO()));
        TagRepositoryImpl tags = new TagRepositoryImpl(); inject(tags, "tagMapper", tagMapper);
        assertEquals(2L, tags.selectPage(TENANT, 1, 10, "math").getLeft());
        assertEquals(2L, tags.selectPage(TENANT, null, null, " ").getLeft());
        when(tagMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new TagDO());
        assertNotNull(tags.selectById(TENANT, 1L)); assertNotNull(tags.selectByName(TENANT, "math"));
        assertEquals(1, tags.selectAll(TENANT).size());
        TagBO tag = new TagBO(); tag.setName("math");
        when(tagMapper.insertSelective(any(TagDO.class))).thenAnswer(i -> { ((TagDO) i.getArgument(0)).setId(9L); return 1; });
        assertEquals(9L, tags.insert(TENANT, tag).getId());
        when(tagMapper.updateByQuery(any(TagDO.class), any(QueryWrapper.class))).thenReturn(1);
        when(tagMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
        assertTrue(tags.update(TENANT, tag)); assertTrue(tags.deleteById(TENANT, 9L));
        when(tagMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertNull(tags.selectById(TENANT, 99L));
        when(tagMapper.updateByQuery(any(TagDO.class), any(QueryWrapper.class))).thenReturn(0);
        when(tagMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(0);
        assertFalse(tags.update(TENANT, tag)); assertFalse(tags.deleteById(TENANT, 99L));

        RecordMapper recordMapper = mock(RecordMapper.class);
        when(recordMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        when(recordMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new RecordDO()));
        RecordRepositoryImpl records = new RecordRepositoryImpl(); inject(records, "recordMapper", recordMapper);
        assertEquals(1, records.selectPage(TENANT, 1, 5, 3L).getRight().size());
        assertEquals(1, records.selectPage(TENANT, null, null, null).getRight().size());
        when(recordMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new RecordDO());
        assertNotNull(records.selectById(TENANT, 4L));
        RecordBO record = new RecordBO(); record.setEventId(3L);
        when(recordMapper.insertSelective(any(RecordDO.class))).thenAnswer(i -> { ((RecordDO) i.getArgument(0)).setId(8L); return 1; });
        assertEquals(8L, records.insert(TENANT, record).getId());
        when(recordMapper.updateByQuery(any(RecordDO.class), any(QueryWrapper.class))).thenReturn(1);
        when(recordMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
        assertTrue(records.update(TENANT, record)); assertTrue(records.deleteById(TENANT, 8L));
        when(recordMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertNull(records.selectById(TENANT, 99L));
        when(recordMapper.updateByQuery(any(RecordDO.class), any(QueryWrapper.class))).thenReturn(0);
        when(recordMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(0);
        assertFalse(records.update(TENANT, record)); assertFalse(records.deleteById(TENANT, 99L));
        }
    }

    @Test
    void exercisesTimelineDetailsDefaultsAndTenantPredicates() throws Exception {
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(TimelineEventBO.class))).thenReturn(List.of(new TimelineEventBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(TimelineEventBO.class))).thenReturn(new TimelineEventBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(com.shiyu.ai.record.implementation.persistence.dataobject.TimelineEventDO.class))).thenReturn(new TimelineEventDO());
        TimelineEventMapper events = mock(TimelineEventMapper.class);
        RecordMapper records = mock(RecordMapper.class);
        MediaMapper media = mock(MediaMapper.class);
        TimelineEventRepositoryImpl repository = new TimelineEventRepositoryImpl();
        inject(repository, "timelineEventMapper", events); inject(repository, "recordMapper", records); inject(repository, "mediaMapper", media);
        when(events.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        when(events.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new TimelineEventDO()));
        assertEquals(1L, repository.selectPage(TENANT, 1, 5, 2L).getLeft());
        assertEquals(1, repository.selectByProfileId(TENANT, 2L).size());
        when(events.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertNull(repository.selectByIdWithDetails(TENANT, 10L));
        TimelineEventDO event = new TimelineEventDO(); event.setId(10L); event.setEventTime(LocalDateTime.now());
        RecordDO record = new RecordDO(); record.setId(20L);
        when(events.selectOneByQuery(any(QueryWrapper.class))).thenReturn(event);
        when(records.selectOneByQuery(any(QueryWrapper.class))).thenReturn(record);
        when(media.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertNotNull(repository.selectByIdWithDetails(TENANT, 10L));
        TimelineEventBO input = new TimelineEventBO();
        when(events.insert(any(TimelineEventDO.class))).thenAnswer(i -> { ((TimelineEventDO) i.getArgument(0)).setId(11L); return 1; });
        assertEquals(11L, repository.insert(TENANT, input).getId());
        when(events.updateByQuery(any(TimelineEventDO.class), any(QueryWrapper.class))).thenReturn(1);
        when(events.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
        assertTrue(repository.update(TENANT, input)); assertTrue(repository.deleteById(TENANT, 11L));
        when(events.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertEquals(1, repository.selectPage(TENANT, null, null, null).getRight().size());
        when(events.updateByQuery(any(TimelineEventDO.class), any(QueryWrapper.class))).thenReturn(0);
        when(events.deleteByQuery(any(QueryWrapper.class))).thenReturn(0);
        assertFalse(repository.update(TENANT, input)); assertFalse(repository.deleteById(TENANT, 99L));
        }
    }

    @Test
    void exercisesMediaAndProfileRepositoriesIncludingGenderLabels() throws Exception {
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(MediaBO.class))).thenReturn(List.of(new MediaBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(MediaBO.class))).thenReturn(new MediaBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(MediaDO.class))).thenReturn(new MediaDO());
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(ProfileBO.class))).thenReturn(List.of(new ProfileBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(ProfileBO.class))).thenReturn(new ProfileBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(ProfileDO.class))).thenReturn(new ProfileDO());
            MediaMapper mediaMapper = mock(MediaMapper.class);
            when(mediaMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
            when(mediaMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new MediaDO()));
            MediaRepositoryImpl media = new MediaRepositoryImpl(); inject(media, "mediaMapper", mediaMapper);
            assertEquals(1, media.selectPage(TENANT, 1, 5, 2L).getRight().size());
            assertEquals(1, media.selectPage(TENANT, null, null, null).getRight().size());
            when(mediaMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new MediaDO());
            assertNotNull(media.selectById(TENANT, 3L));
            MediaBO mediaBO = new MediaBO();
            when(mediaMapper.insertSelective(any(MediaDO.class))).thenAnswer(i -> { ((MediaDO) i.getArgument(0)).setId(4L); return 1; });
            assertEquals(4L, media.insert(TENANT, mediaBO).getId());
            when(mediaMapper.updateByQuery(any(MediaDO.class), any(QueryWrapper.class))).thenReturn(1);
            when(mediaMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
            assertTrue(media.update(TENANT, mediaBO)); assertTrue(media.deleteById(TENANT, 4L));
            when(mediaMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            assertNull(media.selectById(TENANT, 99L));
            when(mediaMapper.updateByQuery(any(MediaDO.class), any(QueryWrapper.class))).thenReturn(0);
            when(mediaMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(0);
            assertFalse(media.update(TENANT, mediaBO)); assertFalse(media.deleteById(TENANT, 99L));

            ProfileMapper profileMapper = mock(ProfileMapper.class);
            when(profileMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
            when(profileMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new ProfileDO()));
            ProfileRepositoryImpl profiles = new ProfileRepositoryImpl(); inject(profiles, "profileMapper", profileMapper);
            assertEquals(1, profiles.selectPage(TENANT, 1, 5, "creator").getRight().size());
            assertEquals(1, profiles.selectPage(TENANT, null, null, " ").getRight().size());
            ProfileBO profileBO = new ProfileBO(); profileBO.setGender(1);
            when(profileMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new ProfileDO());
            assertNotNull(profiles.selectById(TENANT, 6L));
            when(profileMapper.insertSelective(any(ProfileDO.class))).thenAnswer(i -> { ((ProfileDO) i.getArgument(0)).setId(7L); return 1; });
            assertEquals(7L, profiles.insert(TENANT, profileBO).getId());
            when(profileMapper.updateByQuery(any(ProfileDO.class), any(QueryWrapper.class))).thenReturn(1);
            when(profileMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
            assertTrue(profiles.update(TENANT, profileBO)); assertTrue(profiles.deleteById(TENANT, 7L));
            when(profileMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            assertNull(profiles.selectById(TENANT, 99L));
            when(profileMapper.updateByQuery(any(ProfileDO.class), any(QueryWrapper.class))).thenReturn(0);
            when(profileMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(0);
            assertFalse(profiles.update(TENANT, profileBO)); assertFalse(profiles.deleteById(TENANT, 99L));
        }
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
