package com.shiyu.ai.auth.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.auth.port.repository.DictRepository;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.auth.service.convert.DictConverter;
import com.shiyu.ai.auth.domain.model.DictBO;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DictServiceImpl implements DictService {
    @Override public Pair<Long, List<DictVO>> pageView(ActorContext actor, Number n, Number s) { actor = requireActor(actor); var p=getAll(actor, n,s); return Pair.of(p.getLeft(), DictConverter.INSTANCE.toVOList(p.getRight())); }
    @Override public List<DictVO> byTypeView(ActorContext actor, String type) { actor = requireActor(actor); return DictConverter.INSTANCE.toVOList(getByDictType(actor, type)); }
    @Override public DictVO create(ActorContext actor, DictRequest r) { return DictConverter.INSTANCE.toVO(create(requireActor(actor), toBO(r))); }
    @Override public DictVO update(ActorContext actor, Long id, DictRequest r) { DictBO b=toBO(r); b.setId(id); return DictConverter.INSTANCE.toVO(update(requireActor(actor), b)); }
    private DictBO toBO(DictRequest r) { DictBO b=new DictBO(); b.setDictType(r.getDictType()); b.setDictLabel(r.getDictLabel()); b.setDictValue(r.getDictValue()); b.setDictSort(r.getDictSort()); b.setCssClass(r.getCssClass()); b.setListClass(r.getListClass()); b.setIsDefault(r.getIsDefault()); b.setRemark(r.getRemark()); b.setStatus(r.getStatus()); return b; }

    private final DictRepository dictRepository;

    private final Cache<String, List<DictBO>> dictTypeCache;

    public DictServiceImpl(DictRepository dictRepository) {
        this.dictRepository = dictRepository;
        this.dictTypeCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private Pair<Long, List<DictBO>> getAll(ActorContext actor, Number pageNo, Number pageSize) {
        return dictRepository.selectPage(actor.tenantId(), pageNo, pageSize);
    }

    private DictBO getById(ActorContext actor, Long id) {
        return dictRepository.selectById(actor.tenantId(), id);
    }

    private List<DictBO> getByDictType(ActorContext actor, String dictType) {
        String cacheKey = cacheKey(actor, dictType);
        List<DictBO> cached = dictTypeCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("瀛楀吀缂撳瓨鍛戒腑: {}", dictType);
            return cached;
        }
        List<DictBO> list = dictRepository.selectByDictType(actor.tenantId(), dictType);
        if (list != null) {
            dictTypeCache.put(cacheKey, list);
        }
        return list;
    }

    private DictBO create(ActorContext actor, DictBO dictBO) {
        Long targetTenantId = actor.tenantId().value();
        if (targetTenantId == null) {
            throw new IllegalArgumentException("目标租户不在当前租户可管理范围内");
        }
        dictBO.setTenantId(targetTenantId);
        DictBO created = dictRepository.create(dictBO);
        dictTypeCache.invalidate(cacheKey(actor, dictBO.getDictType()));
        return created;
    }

    private DictBO update(ActorContext actor, DictBO dictBO) {
        if (dictBO == null || dictBO.getId() == null) {
            return null;
        }
        DictBO existing = dictRepository.selectById(actor.tenantId(), dictBO.getId());
        if (existing == null) {
            return null;
        }
        Long targetTenantId = actor.tenantId().value();
        if (targetTenantId == null || !targetTenantId.equals(existing.getTenantId())) {
            throw new IllegalArgumentException("字典不属于目标租户或超出当前租户范围");
        }
        dictBO.setTenantId(existing.getTenantId());
        DictBO updated = dictRepository.update(dictBO);
        dictTypeCache.invalidate(cacheKey(actor, dictBO.getDictType()));
        return updated;
    }

    @Override
    public void deleteById(ActorContext actor, Long id) {
        actor = requireActor(actor);
        DictBO existing = dictRepository.selectById(actor.tenantId(), id);
        if (existing != null) {
            dictRepository.deleteById(actor.tenantId(), id);
            dictTypeCache.invalidate(cacheKey(actor, existing.getDictType()));
        }
    }

    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        actor = requireActor(actor);
        for (Long id : ids) {
            DictBO existing = dictRepository.selectById(actor.tenantId(), id);
            if (existing != null) {
                dictRepository.deleteById(actor.tenantId(), id);
                dictTypeCache.invalidate(cacheKey(actor, existing.getDictType()));
            }
        }
    }

    private String cacheKey(ActorContext actor, String dictType) {
        return "tenant:" + actor.tenantId().value() + ":" + dictType;
    }

    private ActorContext requireActor(ActorContext actor) {
        if (actor == null || actor.tenantId() == null || actor.tenantId().value() <= 0) {
            throw new IllegalArgumentException("actor tenant context is required");
        }
        return actor;
    }
}
