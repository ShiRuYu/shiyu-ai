package com.shiyu.ai.agent.biz.common.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.dal.repository.DictRepository;
import com.shiyu.ai.agent.biz.common.service.DictService;
import com.shiyu.ai.model.bo.DictBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictRepository dictRepository;

    private final Cache<String, List<DictBO>> dictTypeCache;

    public DictServiceImpl() {
        this.dictTypeCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Override
    public Pair<Long, List<DictBO>> getAll(Number pageNo, Number pageSize) {
        return dictRepository.selectPage(pageNo, pageSize);
    }

    @Override
    public DictBO getById(Long id) {
        return dictRepository.selectById(id);
    }

    @Override
    public List<DictBO> getByDictType(String dictType) {
        List<DictBO> cached = dictTypeCache.getIfPresent(dictType);
        if (cached != null) {
            log.debug("字典缓存命中: {}", dictType);
            return cached;
        }
        List<DictBO> list = dictRepository.selectByDictType(dictType);
        if (list != null) {
            dictTypeCache.put(dictType, list);
        }
        return list;
    }

    @Override
    public DictBO create(DictBO dictBO) {
        DictBO created = dictRepository.create(dictBO);
        dictTypeCache.invalidate(dictBO.getDictType());
        return created;
    }

    @Override
    public DictBO update(DictBO dictBO) {
        DictBO updated = dictRepository.update(dictBO);
        dictTypeCache.invalidate(dictBO.getDictType());
        return updated;
    }

    @Override
    public void deleteById(Long id) {
        DictBO existing = dictRepository.selectById(id);
        if (existing != null) {
            dictRepository.deleteById(id);
            dictTypeCache.invalidate(existing.getDictType());
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            DictBO existing = dictRepository.selectById(id);
            if (existing != null) {
                dictRepository.deleteById(id);
                dictTypeCache.invalidate(existing.getDictType());
            }
        }
    }
}
