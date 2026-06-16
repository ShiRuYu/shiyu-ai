package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.biz.auth.util.TenantWorkspaceHelper;
import com.shiyu.ai.agent.dal.dataobject.agent.AiPlatformDO;
import com.shiyu.ai.agent.dal.mapper.agent.AiPlatformMapper;
import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import com.shiyu.ai.agent.domain.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiPlatformRepository {

    @Resource
    private AiPlatformMapper aiPlatformMapper;

    public Pair<Long, List<AiPlatformBO>> selectPage(Number pageNo, Number pageSize, String name, String code) {
        QueryWrapper countWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(countWrapper);
        countWrapper.eq(AiPlatformDO::getDelFlag, "0");
        if (name != null) countWrapper.like(AiPlatformDO::getName, name);
        if (code != null) countWrapper.like(AiPlatformDO::getCode, code);
        long count = aiPlatformMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
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

    public List<AiPlatformBO> selectAllEnabled() {
        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, "1");
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiPlatformBO.class);
    }

    public AiPlatformBO selectById(Long id) {
        AiPlatformDO platformDO = aiPlatformMapper.selectOneById(id);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO selectByCode(String code) {
        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        queryWrapper.eq(AiPlatformDO::getCode, code);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO selectDefault() {
        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        queryWrapper.eq(AiPlatformDO::getIsDefault, "Y");
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, "1");
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    public AiPlatformBO create(AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        aiPlatformMapper.insertSelective(platformDO);
        bo.setId(platformDO.getId());
        return bo;
    }

    public AiPlatformBO update(AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        aiPlatformMapper.update(platformDO);
        return bo;
    }

    public void deleteById(Long id) {
        aiPlatformMapper.deleteById(id);
    }

    public List<IdNameOptionVO> selectOptions() {
        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, "1");
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);
        queryWrapper.orderBy(AiPlatformDO::getId, true);
        queryWrapper.select(AiPlatformDO::getId, AiPlatformDO::getName, AiPlatformDO::getCode);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return list.stream()
                .map(p -> IdNameOptionVO.builder().id(p.getId()).name(p.getName()).code(p.getCode()).build())
                .collect(Collectors.toList());
    }

    public void clearDefaultExcept(Long excludeId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
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
