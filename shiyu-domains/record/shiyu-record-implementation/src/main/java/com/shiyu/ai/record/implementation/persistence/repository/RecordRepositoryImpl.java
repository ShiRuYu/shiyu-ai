package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.record.implementation.persistence.dataobject.RecordDO;
import com.shiyu.ai.record.implementation.persistence.mapper.RecordMapper;
import com.shiyu.ai.record.domain.model.RecordBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordRepositoryImpl implements com.shiyu.ai.record.port.repository.RecordRepository {

    @Resource
    private RecordMapper recordMapper;

    public Pair<Long, List<RecordBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long eventId) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(RecordDO::getTenantId, tenantId.value());
        if (eventId != null) {
            countWrapper.eq(RecordDO::getEventId, eventId);
        }
        long total = recordMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RecordDO::getTenantId, tenantId.value());
        if (eventId != null) {
            queryWrapper.eq(RecordDO::getEventId, eventId);
        }
        queryWrapper.orderBy(RecordDO::getId, false);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<RecordDO> doList = recordMapper.selectListByQuery(queryWrapper);
        List<RecordBO> boList = MapstructUtils.convert(doList, RecordBO.class);
        return Pair.of(total, boList);
    }

    public RecordBO selectById(TenantId tenantId, Long id) {
        RecordDO d = recordMapper.selectOneByQuery(QueryWrapper.create()
                .eq(RecordDO::getTenantId, tenantId.value())
                .eq(RecordDO::getId, id));
        return MapstructUtils.convert(d, RecordBO.class);
    }

    public RecordBO insert(TenantId tenantId, RecordBO recordBO) {
        RecordDO d = MapstructUtils.convert(recordBO, RecordDO.class);
        d.setTenantId(tenantId.value());
        recordMapper.insertSelective(d);
        recordBO.setId(d.getId());
        return recordBO;
    }

    public boolean update(TenantId tenantId, RecordBO recordBO) {
        RecordDO d = MapstructUtils.convert(recordBO, RecordDO.class);
        d.setTenantId(tenantId.value());
        return recordMapper.updateByQuery(d, QueryWrapper.create()
                .eq(RecordDO::getTenantId, tenantId.value())
                .eq(RecordDO::getId, recordBO.getId())) > 0;
    }

    public boolean deleteById(TenantId tenantId, Long id) {
        return recordMapper.deleteByQuery(QueryWrapper.create()
                .eq(RecordDO::getTenantId, tenantId.value())
                .eq(RecordDO::getId, id)) > 0;
    }
}
