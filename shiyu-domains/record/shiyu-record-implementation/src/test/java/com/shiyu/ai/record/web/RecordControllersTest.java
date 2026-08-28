package com.shiyu.ai.record.web;

import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.request.*;
import com.shiyu.ai.record.service.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecordControllersTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(71), new UserId(72), false);

    @Test
    void exposesTenantAwareCrudEndpointsForAllRecordResources() throws Exception {
        MediaService media = mock(MediaService.class);
        MediaController mediaController = inject(new MediaController(), "mediaService", media);
        when(media.pageView(any(), any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(media.detailView(any(), anyLong())).thenReturn(null);
        when(media.create(any(), any())).thenReturn(null);
        when(media.update(any(), anyLong(), any())).thenReturn(true);
        when(media.delete(any(), anyLong())).thenReturn(true);

        ProfileService profile = mock(ProfileService.class);
        ProfileController profileController = inject(new ProfileController(), "profileService", profile);
        when(profile.pageView(any(), any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(profile.detailView(any(), anyLong())).thenReturn(null);
        when(profile.create(any(), any())).thenReturn(null);
        when(profile.update(any(), anyLong(), any())).thenReturn(true);
        when(profile.delete(any(), anyLong())).thenReturn(true);

        RecordService record = mock(RecordService.class);
        RecordController recordController = inject(new RecordController(), "recordService", record);
        when(record.pageView(any(), any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(record.detailView(any(), anyLong())).thenReturn(null);
        when(record.create(any(), any())).thenReturn(null);
        when(record.update(any(), anyLong(), any())).thenReturn(true);
        when(record.delete(any(), anyLong())).thenReturn(true);

        TagService tag = mock(TagService.class);
        TagController tagController = inject(new TagController(), "tagService", tag);
        when(tag.pageView(any(), any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(tag.allView(any())).thenReturn(List.of());
        when(tag.detailView(any(), anyLong())).thenReturn(null);
        when(tag.create(any(), any())).thenReturn(null);
        when(tag.update(any(), anyLong(), any())).thenReturn(true);
        when(tag.delete(any(), anyLong())).thenReturn(true);

        TimelineEventService timeline = mock(TimelineEventService.class);
        TimelineEventController timelineController = inject(new TimelineEventController(), "timelineEventService", timeline);
        when(timeline.pageView(any(), any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(timeline.detailView(any(), anyLong())).thenReturn(null);
        when(timeline.create(any(), any())).thenReturn(null);
        when(timeline.update(any(), anyLong(), any())).thenReturn(true);
        when(timeline.delete(any(), anyLong())).thenReturn(true);
        when(timeline.timelineView(any(), anyLong())).thenReturn(List.of());

        PageQuery query = new PageQuery();
        try (MockedStatic<ActorContextHttpAdapter> actor = mockStatic(ActorContextHttpAdapter.class)) {
            actor.when(ActorContextHttpAdapter::currentActor).thenReturn(ACTOR);
            assertNotNull(mediaController.getPage(query, 1L));
            assertNotNull(mediaController.getById(1L));
            assertNotNull(mediaController.create(new MediaRequest()));
            assertNotNull(mediaController.update(1L, new MediaRequest()));
            assertNotNull(mediaController.delete(1L));

            assertNotNull(profileController.getPage(query, "u"));
            assertNotNull(profileController.getById(1L));
            assertNotNull(profileController.create(new ProfileRequest()));
            assertNotNull(profileController.update(1L, new ProfileRequest()));
            assertNotNull(profileController.delete(1L));

            assertNotNull(recordController.getPage(query, 2L));
            assertNotNull(recordController.getById(1L));
            assertNotNull(recordController.create(new RecordRequest()));
            assertNotNull(recordController.update(1L, new RecordRequest()));
            assertNotNull(recordController.delete(1L));

            assertNotNull(tagController.getPage(query, "math"));
            assertNotNull(tagController.getAll());
            assertNotNull(tagController.getById(1L));
            assertNotNull(tagController.create(new TagRequest()));
            assertNotNull(tagController.update(1L, new TagRequest()));
            assertNotNull(tagController.delete(1L));

            assertNotNull(timelineController.getPage(query, 1L));
            assertNotNull(timelineController.getById(1L));
            assertNotNull(timelineController.create(new TimelineEventRequest()));
            assertNotNull(timelineController.update(1L, new TimelineEventRequest()));
            assertNotNull(timelineController.delete(1L));
            assertNotNull(timelineController.getTimelineByProfileId(1L));
        }
        verify(media).pageView(ACTOR, query.getPageNum(), query.getPageSize(), 1L);
        verify(profile).pageView(ACTOR, query.getPageNum(), query.getPageSize(), "u");
        verify(record).pageView(ACTOR, query.getPageNum(), query.getPageSize(), 2L);
        verify(tag).pageView(ACTOR, query.getPageNum(), query.getPageSize(), "math");
        verify(timeline).timelineView(ACTOR, 1L);
    }

    private static <T> T inject(T target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
        return target;
    }
}
