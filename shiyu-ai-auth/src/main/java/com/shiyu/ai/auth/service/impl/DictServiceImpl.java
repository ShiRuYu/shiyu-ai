package com.shiyu.ai.auth.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.auth.port.repository.DictRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.auth.service.convert.DictConverter;
import com.shiyu.ai.auth.domain.model.DictBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DictServiceImpl implements DictService {
    @Override public Pair<Long, List<DictVO>> pageView(Number n, Number s) { var p=getAll(n,s); return Pair.of(p.getLeft(), DictConverter.INSTANCE.toVOList(p.getRight())); }
    @Override public List<DictVO> byTypeView(String type) { return DictConverter.INSTANCE.toVOList(getByDictType(type)); }
    @Override public DictVO create(DictRequest r) { return DictConverter.INSTANCE.toVO(create(toBO(r))); }
    @Override public DictVO update(Long id, DictRequest r) { DictBO b=toBO(r); b.setId(id); return DictConverter.INSTANCE.toVO(update(b)); }
    private DictBO toBO(DictRequest r) { DictBO b=new DictBO(); b.setDictType(r.getDictType()); b.setDictLabel(r.getDictLabel()); b.setDictValue(r.getDictValue()); b.setDictSort(r.getDictSort()); b.setCssClass(r.getCssClass()); b.setListClass(r.getListClass()); b.setIsDefault(r.getIsDefault()); b.setRemark(r.getRemark()); b.setStatus(r.getStatus()); return b; }

    @Resource
    private DictRepository dictRepository;

    @Resource
    private TenantRepository tenantRepository;

    private final Cache<String, List<DictBO>> dictTypeCache;

    public DictServiceImpl() {
        this.dictTypeCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private Pair<Long, List<DictBO>> getAll(Number pageNo, Number pageSize) {
        return dictRepository.selectPage(pageNo, pageSize);
    }

    private DictBO getById(Long id) {
        return dictRepository.selectById(id);
    }

    private List<DictBO> getByDictType(String dictType) {
        String cacheKey = cacheKey(dictType);
        List<DictBO> cached = dictTypeCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("瀛楀吀缂撳瓨鍛戒腑: {}", dictType);
            return cached;
        }
        List<DictBO> list = dictRepository.selectByDictType(dictType);
        if (list != null) {
            dictTypeCache.put(cacheKey, list);
        }
        return list;
    }

    private DictBO create(DictBO dictBO) {
        Long targetTenantId = resolveTargetTenantId(dictBO == null ? null : dictBO.getTenantId());
        if (targetTenantId == null) {
            throw new IllegalArgumentException("目标租户不在当前租户可管理范围内");
        }
        dictBO.setTenantId(targetTenantId);
        DictBO created = dictRepository.create(dictBO);
        dictTypeCache.invalidate(cacheKey(dictBO.getDictType()));
        return created;
    }

    private DictBO update(DictBO dictBO) {
        if (dictBO == null || dictBO.getId() == null) {
            return null;
        }
        DictBO existing = dictRepository.selectById(dictBO.getId());
        if (existing == null) {
            return null;
        }
        Long targetTenantId = resolveTargetTenantId(dictBO.getTenantId());
        if (targetTenantId == null || !targetTenantId.equals(existing.getTenantId())) {
            throw new IllegalArgumentException("字典不属于目标租户或超出当前租户范围");
        }
        dictBO.setTenantId(existing.getTenantId());
        DictBO updated = dictRepository.update(dictBO);
        dictTypeCache.invalidate(cacheKey(dictBO.getDictType()));
        return updated;
    }

    @Override
    public void deleteById(Long id) {
        DictBO existing = dictRepository.selectById(id);
        if (existing != null) {
            ensureTenantVisible(existing.getTenantId());
            dictRepository.deleteById(id);
            dictTypeCache.invalidate(cacheKey(existing.getDictType()));
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            DictBO existing = dictRepository.selectById(id);
            if (existing != null) {
                dictRepository.deleteById(id);
                dictTypeCache.invalidate(cacheKey(existing.getDictType()));
            }
        }
    }

    private String cacheKey(String dictType) {
        return "tenant:" + LoginContextHolder.getCurrentTenantId() + ":" + dictType;
    }

    private Long resolveTargetTenantId(Long requestedTenantId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null) {
            return null;
        }
        Long targetTenantId = requestedTenantId == null ? currentTenantId : requestedTenantId;
        return currentTenantId.equals(targetTenantId) ? targetTenantId : null;
    }

    private void ensureTenantVisible(Long tenantId) {
        if (resolveTargetTenantId(tenantId) == null) {
            throw new IllegalArgumentException("字典不属于当前租户可管理范围");
        }
    }
}
