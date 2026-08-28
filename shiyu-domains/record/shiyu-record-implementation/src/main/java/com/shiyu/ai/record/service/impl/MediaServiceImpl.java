package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.record.domain.model.MediaBO;
import com.shiyu.ai.record.port.repository.MediaRepository;
import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.record.vo.MediaVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;

    public MediaServiceImpl(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }
    @Override public Pair<Long,List<MediaVO>> pageView(ActorContext actor,Number n,Number s,Long recordId){actor=requireActor(actor);if(n==null||n.intValue()<1)n=1;if(s==null||s.intValue()<1)s=10;var p=mediaRepository.selectPage(actor.tenantId(),n,s,recordId);return Pair.of(p.getLeft(),MapstructUtils.convert(p.getRight(),MediaVO.class));}
    @Override public MediaVO detailView(ActorContext actor,Long id){return MapstructUtils.convert(mediaRepository.selectById(requireActor(actor).tenantId(),id),MediaVO.class);}
    @Override public MediaVO create(ActorContext actor,MediaRequest r){actor=requireActor(actor);MediaBO b=new MediaBO();b.setRecordId(r.getRecordId());b.setUrl(r.getUrl());b.setType(r.getMediaType());return MapstructUtils.convert(mediaRepository.insert(actor.tenantId(),b),MediaVO.class);}
    @Override public boolean update(ActorContext actor,Long id,MediaRequest r){actor=requireActor(actor);MediaBO b=mediaRepository.selectById(actor.tenantId(),id);if(b==null)return false;b.setUrl(r.getUrl());b.setType(r.getMediaType());return mediaRepository.update(actor.tenantId(),b);}
    @Override public boolean delete(ActorContext actor,Long id){return mediaRepository.deleteById(requireActor(actor).tenantId(),id);}
    private static ActorContext requireActor(ActorContext actor){return Objects.requireNonNull(actor,"actor is required");}
}
