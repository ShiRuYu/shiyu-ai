package com.shiyu.ai.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.DictDO;
import com.shiyu.ai.agent.dal.mapper.DictMapper;
import com.shiyu.ai.agent.domain.bo.DictBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典数据仓储层
 */
@Component
public class DictRepository {

    @Resource
    private DictMapper dictMapper;

    /**
     * 分页查询字典列表
     */
    public Pair<Long, List<DictBO>> getAll(Number pageNumber, Number pageSize) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getDelFlag, "0");
        queryWrapper.orderBy(DictDO::getDictType, true);
        queryWrapper.orderBy(DictDO::getDictSort, true);

        if (pageNumber != null && pageSize != null) {
            queryWrapper.limit((pageNumber.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<DictDO> dictList = dictMapper.selectListByQuery(queryWrapper);
        long count = dictMapper.selectCountByQuery(new QueryWrapper().eq(DictDO::getDelFlag, "0"));
        
        return Pair.of(count, MapstructUtils.convert(dictList, DictBO.class));
    }

    /**
     * 根据ID查询字典
     */
    public DictBO getById(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getId, id);
        queryWrapper.eq(DictDO::getDelFlag, "0");
        DictDO dictDO = dictMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(dictDO, DictBO.class);
    }

    /**
     * 根据字典类型查询字典列表
     */
    public List<DictBO> getByDictType(String dictType) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getDictType, dictType);
        queryWrapper.eq(DictDO::getStatus, "1");
        queryWrapper.eq(DictDO::getDelFlag, "0");
        queryWrapper.orderBy(DictDO::getDictSort, true);
        List<DictDO> dictList = dictMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(dictList, DictBO.class);
    }

    /**
     * 创建字典
     */
    public DictBO create(DictBO dictBO) {
        DictDO dictDO = MapstructUtils.convert(dictBO, DictDO.class);
        dictDO.setDelFlag("0");
        dictMapper.insert(dictDO);
        return MapstructUtils.convert(dictDO, DictBO.class);
    }

    /**
     * 更新字典
     */
    public DictBO update(DictBO dictBO) {
        DictDO dictDO = MapstructUtils.convert(dictBO, DictDO.class);
        dictMapper.update(dictDO);
        return MapstructUtils.convert(dictDO, DictBO.class);
    }

    /**
     * 删除字典（逻辑删除）
     */
    public void deleteById(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getId, id);
        DictDO dictDO = new DictDO();
        dictDO.setDelFlag("1");
        dictMapper.updateByQuery(dictDO, queryWrapper);
    }

    /**
     * 批量删除字典（逻辑删除）
     */
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(DictDO::getId, ids);
        DictDO dictDO = new DictDO();
        dictDO.setDelFlag("1");
        dictMapper.updateByQuery(dictDO, queryWrapper);
    }
}
