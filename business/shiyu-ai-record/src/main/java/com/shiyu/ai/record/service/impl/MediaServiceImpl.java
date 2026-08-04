package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.port.repository.MediaRepository;
import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.vo.MediaVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.domain.model.MediaBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {
    @Override public Pair<Long, List<MediaVO>> pageView(Number n, Number s, Long recordId) { var p=getPage(n,s,recordId); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), MediaVO.class)); }
    @Override public MediaVO detailView(Long id) { return MapstructUtils.convert(getById(id), MediaVO.class); }
    @Override public MediaVO create(MediaRequest r) { MediaBO b=new MediaBO(); b.setRecordId(r.getRecordId()); b.setUrl(r.getUrl()); b.setType(r.getMediaType()); return MapstructUtils.convert(create(b), MediaVO.class); }
    @Override public boolean update(Long id, MediaRequest r) { MediaBO b=getById(id); if(b==null)return false; b.setUrl(r.getUrl()); b.setType(r.getMediaType()); return update(b); }

    @Resource
    private MediaRepository mediaRepository;

    private Pair<Long, List<MediaBO>> getPage(Number pageNo, Number pageSize, Long recordId) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return mediaRepository.selectPage(pageNo, pageSize, recordId);
    }

    private MediaBO getById(Long id) {
        return mediaRepository.selectById(id);
    }

    private MediaBO create(MediaBO mediaBO) {
        return mediaRepository.insert(mediaBO);
    }

    private boolean update(MediaBO mediaBO) {
        return mediaRepository.update(mediaBO);
    }

    @Override
    public boolean delete(Long id) {
        return mediaRepository.deleteById(id);
    }
}
