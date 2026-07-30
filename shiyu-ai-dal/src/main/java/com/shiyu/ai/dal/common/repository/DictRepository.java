package com.shiyu.ai.dal.common.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.common.dataobject.DictDO;
import com.shiyu.ai.dal.common.mapper.DictMapper;
import com.shiyu.ai.dal.common.bo.DictBO;
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
        countWrapper.eq(DictDO::getDelFlag, "0");
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
        // tenant_id 必须由 DictService 显式设置。
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
        if (dictBO == null || dictBO.getId() == null) {
            return null;
        }
        DictDO dictDO = MapstructUtils.convert(dictBO, DictDO.class);
        // Service 已校验并固定 tenant_id；MyBatis-Flex 仍负责可见租户范围过滤。
        dictMapper.update(dictDO);
        return dictBO;
    }

    /**
     * 删除字典
     */
    public void deleteById(Long id) {
        // DELETE SQL 的 tenant_id 条件由 MyBatis-Flex 租户范围追加。
        dictMapper.deleteById(id);
    }

    /**
     * 批量删除字典
     */
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

}
