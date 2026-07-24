package com.shiyu.ai.dal.model.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.model.dataobject.AiModelDO;
import com.shiyu.ai.dal.model.mapper.AiModelMapper;
import com.shiyu.ai.dal.model.bo.AiModelBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiModelRepository {

    @Resource
    private AiModelMapper aiModelMapper;

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

    public List<AiModelBO> selectByPlatformId(Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, 1);
        queryWrapper.orderBy(AiModelDO::getIsDefault, true);
        queryWrapper.orderBy(AiModelDO::getSort, true);

        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiModelBO.class);
    }

    public AiModelBO selectById(Long id) {
        AiModelDO modelDO = aiModelMapper.selectOneById(id);
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    public AiModelBO selectDefaultByPlatformId(Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getIsDefault, "Y");
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, 1);
        AiModelDO modelDO = aiModelMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    public AiModelBO create(AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        aiModelMapper.insertSelective(modelDO);
        bo.setId(modelDO.getId());
        return bo;
    }

    public AiModelBO update(AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        aiModelMapper.update(modelDO);
        return bo;
    }

    public void deleteById(Long id) {
        aiModelMapper.deleteById(id);
    }

    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            aiModelMapper.deleteById(id);
        }
    }

    public List<IdNameOptionVO> selectOptions(Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, 1);
        if (platformId != null) {
            queryWrapper.eq(AiModelDO::getPlatformId, platformId);
        }
        queryWrapper.orderBy(AiModelDO::getIsDefault, true);
        queryWrapper.orderBy(AiModelDO::getSort, true);
        queryWrapper.select(AiModelDO::getId, AiModelDO::getDisplayName, AiModelDO::getModelName);

        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        return list.stream()
                .map(m -> IdNameOptionVO.builder().id(m.getId()).name(m.getDisplayName()).value(m.getModelName()).build())
                .collect(Collectors.toList());
    }

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
