package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.AiModelDO;
import com.shiyu.ai.agent.dal.mapper.agent.AiModelMapper;
import com.shiyu.ai.agent.domain.bo.AiModelBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 模型数据仓储层
 */
@Component
public class AiModelRepository {

    @Resource
    private AiModelMapper aiModelMapper;

    /**
     * 按平台分页查询模型
     */
    public Pair<Long, List<AiModelBO>> selectPage(Long platformId, Number pageNo, Number pageSize) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AiModelDO::getDelFlag, "0");
        if (platformId != null) {
            countWrapper.eq(AiModelDO::getPlatformId, platformId);
        }
        long count = aiModelMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        if (platformId != null) {
            queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        }
        queryWrapper.orderBy(AiModelDO::getIsDefault, true);
        queryWrapper.orderBy(AiModelDO::getSort, true);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(list, AiModelBO.class));
    }

    /**
     * 查询指定平台下所有启用的模型
     */
    public List<AiModelBO> selectByPlatformId(Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, "1");
        queryWrapper.orderBy(AiModelDO::getIsDefault, true);
        queryWrapper.orderBy(AiModelDO::getSort, true);

        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiModelBO.class);
    }

    /**
     * 根据 ID 查询
     */
    public AiModelBO selectById(Long id) {
        AiModelDO modelDO = aiModelMapper.selectOneById(id);
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    /**
     * 查询平台的默认模型
     */
    public AiModelBO selectDefaultByPlatformId(Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getIsDefault, "Y");
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, "1");
        AiModelDO modelDO = aiModelMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    /**
     * 创建
     */
    public AiModelBO create(AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        aiModelMapper.insertSelective(modelDO);
        bo.setId(modelDO.getId());
        return bo;
    }

    /**
     * 更新
     */
    public AiModelBO update(AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        aiModelMapper.update(modelDO);
        return bo;
    }

    /**
     * 删除
     */
    public void deleteById(Long id) {
        aiModelMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            aiModelMapper.deleteById(id);
        }
    }

    /**
     * 清除平台下其他模型的默认标记
     */
    public void clearDefaultExcept(Long platformId, Long excludeId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getIsDefault, "Y");
        if (excludeId != null) {
            queryWrapper.ne(AiModelDO::getId, excludeId);
        }
        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        for (AiModelDO model : list) {
            model.setIsDefault("N");
            aiModelMapper.update(model);
        }
    }
}
