package com.shiyu.ai.model.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDO;
import com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 负责 AI 平台 的持久化查询、保存和删除，并维护数据访问边界。
 */
@SuppressWarnings("unchecked")
@Component
public class AiPlatformRepositoryImpl
        implements com.shiyu.ai.model.implementation.domain.port.repository.AiPlatformRepository {

    private final PlatformCredentialCipher credentialCipher;

    /**
     * 创建平台仓储。
     *
     * @param credentialCipher 保护数据库中的平台凭证。
     */
    public AiPlatformRepositoryImpl(PlatformCredentialCipher credentialCipher) {
        this.credentialCipher = credentialCipher;
    }

    /**
     * aiPlatformMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AiPlatformMapper aiPlatformMapper;

    public Pair<Long, List<AiPlatformBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String name, String code) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getDelFlag, 0);
        if (name != null) countWrapper.like(AiPlatformDO::getName, name);
        if (code != null) countWrapper.like(AiPlatformDO::getCode, code);
        long count = aiPlatformMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getDelFlag, 0);
        if (name != null) queryWrapper.like(AiPlatformDO::getName, name);
        if (code != null) queryWrapper.like(AiPlatformDO::getCode, code);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);
        queryWrapper.orderBy(AiPlatformDO::getId, true);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit(
                    (pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, list.stream().map(item -> hydrate(tenantId, item)).toList());
    }

    public List<AiPlatformBO> selectAllEnabled(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getDelFlag, 0);
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return list.stream().map(item -> hydrate(tenantId, item)).toList();
    }

    public AiPlatformBO selectById(TenantId tenantId, Long id) {
        AiPlatformDO platformDO =
                aiPlatformMapper.selectOneByQuery(
                        new QueryWrapper()
                                .eq(AiPlatformDO::getTenantId, tenantId.value())
                                .eq(AiPlatformDO::getId, id));
        return hydrate(tenantId, platformDO);
    }

    public AiPlatformBO selectByCode(TenantId tenantId, String code) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getCode, code);
        queryWrapper.eq(AiPlatformDO::getDelFlag, 0);
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return hydrate(tenantId, platformDO);
    }

    public AiPlatformBO selectDefault(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getIsDefault, "Y");
        queryWrapper.eq(AiPlatformDO::getDelFlag, 0);
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return hydrate(tenantId, platformDO);
    }

    public AiPlatformBO create(TenantId tenantId, AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        platformDO.setTenantId(tenantId.value());
        platformDO.setApiKey(credentialCipher.protect(tenantId.value(), bo.getApiKey()));
        aiPlatformMapper.insertSelective(platformDO);
        bo.setId(platformDO.getId());
        return bo;
    }

    public AiPlatformBO update(TenantId tenantId, AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        platformDO.setTenantId(tenantId.value());
        platformDO.setApiKey(credentialCipher.protect(tenantId.value(), bo.getApiKey()));
        aiPlatformMapper.updateByQuery(
                platformDO,
                new QueryWrapper()
                        .eq(AiPlatformDO::getTenantId, tenantId.value())
                        .eq(AiPlatformDO::getId, bo.getId()));
        return bo;
    }

    public void deleteById(TenantId tenantId, Long id) {
        aiPlatformMapper.deleteByQuery(
                new QueryWrapper()
                        .eq(AiPlatformDO::getTenantId, tenantId.value())
                        .eq(AiPlatformDO::getId, id));
    }

    public List<IdNameOptionVO> selectOptions(TenantId tenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq(AiPlatformDO::getTenantId, tenantId.value())
                .eq(AiPlatformDO::getDelFlag, 0);
        queryWrapper.eq(AiPlatformDO::getStatus, 1);
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);
        queryWrapper.orderBy(AiPlatformDO::getId, true);
        queryWrapper.select(AiPlatformDO::getId, AiPlatformDO::getName, AiPlatformDO::getCode);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return list.stream()
                .map(
                        p ->
                                IdNameOptionVO.builder()
                                        .id(p.getId())
                                        .name(p.getName())
                                        .code(p.getCode())
                                        .build())
                .collect(Collectors.toList());
    }

    public void clearDefaultExcept(TenantId tenantId, Long excludeId) {
        QueryWrapper queryWrapper =
                new QueryWrapper().eq(AiPlatformDO::getTenantId, tenantId.value());
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

    private AiPlatformBO hydrate(TenantId tenantId, AiPlatformDO platformDO) {
        if (platformDO == null) return null;
        AiPlatformBO platform = MapstructUtils.convert(platformDO, AiPlatformBO.class);
        if (platform != null && platformDO.getApiKey() != null
                && !platformDO.getApiKey().isEmpty()) {
            platform.setApiKey(credentialCipher.unprotect(
                    tenantId.value(), platformDO.getApiKey()));
        }
        return platform;
    }
}
