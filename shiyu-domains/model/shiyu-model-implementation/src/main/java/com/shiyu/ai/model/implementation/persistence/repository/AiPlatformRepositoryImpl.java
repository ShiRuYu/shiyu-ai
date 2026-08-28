package com.shiyu.ai.model.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDO;
import com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
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
public class AiPlatformRepositoryImpl implements com.shiyu.ai.model.port.repository.AiPlatformRepository {

    @Resource
    private AiPlatformMapper aiPlatformMapper;

    public Pair<Long, List<AiPlatformBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, String code) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getDelFlag, "0");
        if (name != null) countWrapper.like(AiPlatformDO::getName, name);
        if (code != null) countWrapper.like(AiPlatformDO::getCode, code);
        long count = aiPlatformMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getDelFlag, "0");
        if (name != null) queryWrapper.like(AiPlatformDO::getName, name);
        if (code != null) queryWrapper.like(AiPlatformDO::getCode, code);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);
        queryWrapper.orderBy(AiPlatformDO::getId, true);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(list, AiPlatformBO.class));
    }

    public List<AiPlatformBO> selectAllEnabled(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiPlatformBO.class);
    }

    public AiPlatformBO selectById(TenantId tenantId, Long id) {
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(new QueryWrapper().eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getId, id));
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO selectByCode(TenantId tenantId, String code) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getCode, code);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO selectDefault(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getIsDefault, "Y");
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO create(TenantId tenantId, AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        platformDO.setTenantId(tenantId.value());
        aiPlatformMapper.insertSelective(platformDO);
        bo.setId(platformDO.getId());
        return bo;
    }

    public AiPlatformBO update(TenantId tenantId, AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        platformDO.setTenantId(tenantId.value());
        aiPlatformMapper.updateByQuery(platformDO, new QueryWrapper().eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getId, bo.getId()));
        return bo;
    }

    public void deleteById(TenantId tenantId, Long id) {
        aiPlatformMapper.deleteByQuery(new QueryWrapper().eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getId, id));
    }

    public List<IdNameOptionVO> selectOptions(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getTenantId, tenantId.value()).eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);
        queryWrapper.orderBy(AiPlatformDO::getId, true);
        queryWrapper.select(AiPlatformDO::getId, AiPlatformDO::getName, AiPlatformDO::getCode);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return list.stream()
                .map(p -> IdNameOptionVO.builder().id(p.getId()).name(p.getName()).code(p.getCode()).build())
                .collect(Collectors.toList());
    }

    public void clearDefaultExcept(TenantId tenantId, Long excludeId) {
        QueryWrapper queryWrapper = new QueryWrapper().eq(AiPlatformDO::getTenantId, tenantId.value());
        queryWrapper.eq(AiPlatformDO::getIsDefault, "Y");
        if (excludeId != null) {
            queryWrapper.ne(AiPlatformDO::getId, excludeId);
        }
        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        for (AiPlatformDO platform : list) {
            platform.setIsDefault("N");
            aiPlatformMapper.update(platform);
        }
    }
}
