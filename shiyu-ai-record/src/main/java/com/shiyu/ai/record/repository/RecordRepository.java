package com.shiyu.ai.record.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.record.RecordDO;
import com.shiyu.ai.dal.mapper.record.RecordMapper;
import com.shiyu.ai.record.bo.RecordBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordRepository {

    @Resource
    private RecordMapper recordMapper;

    public Pair<Long, List<RecordBO>> selectPage(Number pageNo, Number pageSize, Long eventId) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (eventId != null) {
            countWrapper.eq(RecordDO::getEventId, eventId);
        }
        long total = recordMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
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

    public RecordBO selectById(Long id) {
        RecordDO d = recordMapper.selectOneById(id);
        return MapstructUtils.convert(d, RecordBO.class);
    }

    public RecordBO insert(RecordBO recordBO) {
        RecordDO d = MapstructUtils.convert(recordBO, RecordDO.class);
        recordMapper.insertSelective(d);
        recordBO.setId(d.getId());
        return recordBO;
    }

    public boolean update(RecordBO recordBO) {
        RecordDO d = MapstructUtils.convert(recordBO, RecordDO.class);
        return recordMapper.update(d) > 0;
    }

    public boolean deleteById(Long id) {
        return recordMapper.deleteById(id) > 0;
    }
}
