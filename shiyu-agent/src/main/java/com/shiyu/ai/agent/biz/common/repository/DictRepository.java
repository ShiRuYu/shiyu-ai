package com.shiyu.ai.agent.biz.common.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.common.DictDO;
import com.shiyu.ai.agent.dal.mapper.common.DictMapper;
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
    public Pair<Long, List<DictBO>> selectPage(Number pageNo, Number pageSize) {
        QueryWrapper countWrapper = new QueryWrapper();
        long count = dictMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getDelFlag, "0");
        queryWrapper.orderBy(DictDO::getDictType, true);
        queryWrapper.orderBy(DictDO::getDictSort, true);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<DictDO> dictList = dictMapper.selectListByQuery(queryWrapper);
        
        return Pair.of(count, MapstructUtils.convert(dictList, DictBO.class));
    }

    /**
     * 查询所有字典列表
     */
    public List<DictBO> selectAll() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getDelFlag, "0");
        queryWrapper.orderBy(DictDO::getDictType, true);
        queryWrapper.orderBy(DictDO::getDictSort, true);
        
        List<DictDO> dictList = dictMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(dictList, DictBO.class);
    }

    /**
     * 根据ID查询字典
     */
    public DictBO selectById(Long id) {
        DictDO dictDO = dictMapper.selectOneById(id);
        return MapstructUtils.convert(dictDO, DictBO.class);
    }

    /**
     * 根据字典类型查询字典列表
     */
    public List<DictBO> selectByDictType(String dictType) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(DictDO::getDictType, dictType);
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
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        dictMapper.insertSelective(dictDO);
        dictBO.setId(dictDO.getId());
        return dictBO;
    }

    /**
     * 更新字典
     */
    public DictBO update(DictBO dictBO) {
        DictDO dictDO = MapstructUtils.convert(dictBO, DictDO.class);
        dictMapper.update(dictDO);
        return dictBO;
    }

    /**
     * 删除字典
     */
    public void deleteById(Long id) {
        dictMapper.deleteById(id);
    }

    /**
     * 批量删除字典
     */
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            dictMapper.deleteById(id);
        }
    }
}
