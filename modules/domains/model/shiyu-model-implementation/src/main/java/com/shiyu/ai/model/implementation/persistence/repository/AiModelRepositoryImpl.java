package com.shiyu.ai.model.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiModelDO;
import com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class AiModelRepositoryImpl implements com.shiyu.ai.model.port.repository.AiModelRepository {

    @Resource
    private AiModelMapper aiModelMapper;

    public Pair<Long, List<AiModelBO>> selectPage(TenantId tenantId, Long platformId, Number pageNo, Number pageSize) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getDelFlag, "0");
        if (platformId != null) {
            countWrapper.eq(AiModelDO::getPlatformId, platformId);
        }
        long count = aiModelMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getDelFlag, "0");
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

    public List<AiModelBO> selectByPlatformId(TenantId tenantId, Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, 1);
        queryWrapper.orderBy(AiModelDO::getIsDefault, true);
        queryWrapper.orderBy(AiModelDO::getSort, true);

        List<AiModelDO> list = aiModelMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiModelBO.class);
    }

    public AiModelBO selectById(TenantId tenantId, Long id) {
        AiModelDO modelDO = aiModelMapper.selectOneByQuery(new QueryWrapper().eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getId, id));
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    public AiModelBO selectDefaultByPlatformId(TenantId tenantId, Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getPlatformId, platformId);
        queryWrapper.eq(AiModelDO::getIsDefault, "Y");
        queryWrapper.eq(AiModelDO::getDelFlag, "0");
        queryWrapper.eq(AiModelDO::getStatus, 1);
        AiModelDO modelDO = aiModelMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(modelDO, AiModelBO.class);
    }

    public AiModelBO create(TenantId tenantId, AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        modelDO.setTenantId(tenantId.value());
        aiModelMapper.insertSelective(modelDO);
        bo.setId(modelDO.getId());
        return bo;
    }

    public AiModelBO update(TenantId tenantId, AiModelBO bo) {
        AiModelDO modelDO = MapstructUtils.convert(bo, AiModelDO.class);
        modelDO.setTenantId(tenantId.value());
        aiModelMapper.updateByQuery(modelDO, new QueryWrapper().eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getId, bo.getId()));
        return bo;
    }

    public void deleteById(TenantId tenantId, Long id) {
        aiModelMapper.deleteByQuery(new QueryWrapper().eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getId, id));
    }

    public void deleteByIds(TenantId tenantId, List<Long> ids) {
        for (Long id : ids) {
            deleteById(tenantId, id);
        }
    }

    public List<IdNameOptionVO> selectOptions(TenantId tenantId, Long platformId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getDelFlag, "0");
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

    public void clearDefaultExcept(TenantId tenantId, Long platformId, Long excludeId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiModelDO::getTenantId, tenantId.value()).eq(AiModelDO::getPlatformId, platformId);
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
