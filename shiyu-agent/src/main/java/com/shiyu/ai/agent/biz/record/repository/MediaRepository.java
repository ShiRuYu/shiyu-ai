package com.shiyu.ai.agent.biz.record.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.record.MediaDO;
import com.shiyu.ai.agent.dal.mapper.record.MediaMapper;
import com.shiyu.ai.agent.domain.bo.MediaBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MediaRepository {

    @Resource
    private MediaMapper mediaMapper;

    public Pair<Long, List<MediaBO>> selectPage(Number pageNo, Number pageSize, Long recordId) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (recordId != null) {
            countWrapper.eq(MediaDO::getRecordId, recordId);
        }
        long total = mediaMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (recordId != null) {
            queryWrapper.eq(MediaDO::getRecordId, recordId);
        }
        queryWrapper.orderBy(MediaDO::getSort, true).orderBy(MediaDO::getId, false);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<MediaDO> doList = mediaMapper.selectListByQuery(queryWrapper);
        List<MediaBO> boList = MapstructUtils.convert(doList, MediaBO.class);
        return Pair.of(total, boList);
    }

    public MediaBO selectById(Long id) {
        MediaDO d = mediaMapper.selectOneById(id);
        return MapstructUtils.convert(d, MediaBO.class);
    }

    public MediaBO insert(MediaBO mediaBO) {
        MediaDO d = MapstructUtils.convert(mediaBO, MediaDO.class);
        mediaMapper.insertSelective(d);
        mediaBO.setId(d.getId());
        return mediaBO;
    }

    public boolean update(MediaBO mediaBO) {
        MediaDO d = MapstructUtils.convert(mediaBO, MediaDO.class);
        return mediaMapper.update(d) > 0;
    }

    public boolean deleteById(Long id) {
        return mediaMapper.deleteById(id) > 0;
    }
}
