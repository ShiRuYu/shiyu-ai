package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
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

/**
 * AI 平台数据仓储层
 */
@Component
public class AiPlatformRepository {

    @Resource
    private AiPlatformMapper aiPlatformMapper;

    /**
     * 分页查询
     */
    public Pair<Long, List<AiPlatformBO>> selectPage(Number pageNo, Number pageSize, String name, String code) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(AiPlatformDO::getDelFlag, "0");
        if (name != null) countWrapper.like(AiPlatformDO::getName, name);
        if (code != null) countWrapper.like(AiPlatformDO::getCode, code);
        long count = aiPlatformMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
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

    /**
     * 查询所有启用的平台
     */
    public List<AiPlatformBO> selectAllEnabled() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, "1");
        queryWrapper.orderBy(AiPlatformDO::getIsDefault, true);

        List<AiPlatformDO> list = aiPlatformMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(list, AiPlatformBO.class);
    }

    /**
     * 根据 ID 查询
     */
    public AiPlatformBO selectById(Long id) {
        AiPlatformDO platformDO = aiPlatformMapper.selectOneById(id);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    /**
     * 根据编码查询
     */
    public AiPlatformBO selectByCode(String code) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getCode, code);
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    /**
     * 查询默认平台
     */
    public AiPlatformBO selectDefault() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(AiPlatformDO::getIsDefault, "Y");
        queryWrapper.eq(AiPlatformDO::getDelFlag, "0");
        queryWrapper.eq(AiPlatformDO::getStatus, "1");
        AiPlatformDO platformDO = aiPlatformMapper.selectOneByQuery(queryWrapper);
        return MapstructUtils.convert(platformDO, AiPlatformBO.class);
    }

    /**
     * 创建
     */
    public AiPlatformBO create(AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        aiPlatformMapper.insertSelective(platformDO);
        bo.setId(platformDO.getId());
        return bo;
    }

    /**
     * 更新
     */
    public AiPlatformBO update(AiPlatformBO bo) {
        AiPlatformDO platformDO = MapstructUtils.convert(bo, AiPlatformDO.class);
        aiPlatformMapper.update(platformDO);
        return bo;
    }

    /**
     * 删除
     */
    public void deleteById(Long id) {
        aiPlatformMapper.deleteById(id);
    }

    public List<IdNameOptionVO> selectOptions() {
        QueryWrapper queryWrapper = new QueryWrapper();
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

    /**
     * 清除其他平台的默认标记
     */
    public void clearDefaultExcept(Long excludeId) {
        QueryWrapper queryWrapper = new QueryWrapper();
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
