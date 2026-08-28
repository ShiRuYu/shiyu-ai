package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.record.implementation.persistence.dataobject.MediaDO;
import com.shiyu.ai.record.implementation.persistence.mapper.MediaMapper;
import com.shiyu.ai.record.domain.model.MediaBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MediaRepositoryImpl implements com.shiyu.ai.record.port.repository.MediaRepository {

    @Resource
    private MediaMapper mediaMapper;

    public Pair<Long, List<MediaBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long recordId) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(MediaDO::getTenantId, tenantId.value());
        if (recordId != null) {
            countWrapper.eq(MediaDO::getRecordId, recordId);
        }
        long total = mediaMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(MediaDO::getTenantId, tenantId.value());
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

    public MediaBO selectById(TenantId tenantId, Long id) {
        MediaDO d = mediaMapper.selectOneByQuery(QueryWrapper.create()
                .eq(MediaDO::getTenantId, tenantId.value())
                .eq(MediaDO::getId, id));
        return MapstructUtils.convert(d, MediaBO.class);
    }

    public MediaBO insert(TenantId tenantId, MediaBO mediaBO) {
        MediaDO d = MapstructUtils.convert(mediaBO, MediaDO.class);
        d.setTenantId(tenantId.value());
        mediaMapper.insertSelective(d);
        mediaBO.setId(d.getId());
        return mediaBO;
    }

    public boolean update(TenantId tenantId, MediaBO mediaBO) {
        MediaDO d = MapstructUtils.convert(mediaBO, MediaDO.class);
        d.setTenantId(tenantId.value());
        return mediaMapper.updateByQuery(d, QueryWrapper.create()
                .eq(MediaDO::getTenantId, tenantId.value())
                .eq(MediaDO::getId, mediaBO.getId())) > 0;
    }

    public boolean deleteById(TenantId tenantId, Long id) {
        return mediaMapper.deleteByQuery(QueryWrapper.create()
                .eq(MediaDO::getTenantId, tenantId.value())
                .eq(MediaDO::getId, id)) > 0;
    }
}
