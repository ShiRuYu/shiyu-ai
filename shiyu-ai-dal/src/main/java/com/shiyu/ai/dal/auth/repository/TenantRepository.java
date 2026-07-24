package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.*;
import com.shiyu.ai.dal.auth.mapper.*;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TenantRepository {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantQuotaMapper tenantQuotaMapper;

    public Pair<Long, List<TenantBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            countWrapper.like(TenantDO::getName, name);
        }
        long count = tenantMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(TenantDO::getName, name);
        }
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        queryWrapper.orderBy(TenantDO::getId, true);

        List<TenantDO> tenantDOs = tenantMapper.selectListByQuery(queryWrapper);
        return Pair.of(count, MapstructUtils.convert(tenantDOs, TenantBO.class));
    }

    public List<TenantBO> selectAll() {
        List<TenantDO> tenantDOs = tenantMapper.selectAll();
        return MapstructUtils.convert(tenantDOs, TenantBO.class);
    }

    public TenantBO selectById(Long id) {
        TenantDO tenantDO = tenantMapper.selectOneById(id);
        return MapstructUtils.convert(tenantDO, TenantBO.class);
    }

    public TenantBO insert(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        tenantMapper.insertSelective(tenantDO);
        tenantBO.setId(tenantDO.getId());
        return tenantBO;
    }

    public boolean update(TenantBO tenantBO) {
        TenantDO tenantDO = MapstructUtils.convert(tenantBO, TenantDO.class);
        return tenantMapper.update(tenantDO) > 0;
    }

    public boolean deleteById(Long id) {
        return tenantMapper.deleteById(id) > 0;
    }

    public boolean existsByCode(String code, Long excludeId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(TenantDO::getCode, code);
        if (excludeId != null) {
            qw.ne(TenantDO::getId, excludeId);
        }
        return tenantMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 级联删除租户及其所有关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void cascadeDelete(Long tenantId) {
        // 先找出所有后代子租户，全部级联删除
        List<Long> allIds = selectDescendantIds(tenantId);
        allIds.add(tenantId);

        for (Long id : allIds) {
            QueryWrapper byTenant = QueryWrapper.create().eq("tenant_id", id);
            tenantQuotaMapper.deleteByQuery(QueryWrapper.create().eq(TenantQuotaDO::getTenantId, id));
        }
        tenantMapper.deleteById(tenantId);
    }

    /**
     * 查询指定租户的所有后代租户 ID（包含自身）
     */
    public List<Long> selectDescendantIds(Long rootId) {
        List<TenantDO> all = tenantMapper.selectAll();
        // parentId → childrenId 映射
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (TenantDO t : all) {
            if (t.getParentId() != null) {
                childrenMap.computeIfAbsent(t.getParentId(), k -> new ArrayList<>()).add(t.getId());
            }
        }
        // BFS 收集所有后代
        Set<Long> result = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        if (rootId != null) queue.add(rootId);
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            if (!result.add(cur)) continue; // 已访问
            List<Long> children = childrenMap.get(cur);
            if (children != null) queue.addAll(children);
        }
        return new ArrayList<>(result);
    }
}
