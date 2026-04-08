package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.bo.DictBO;
import com.shiyu.ai.agent.repository.DictRepository;
import com.shiyu.ai.agent.service.DictService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务实现层
 */
@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictRepository dictRepository;

    @Override
    public Pair<Long, List<DictBO>> getAll(Number pageNumber, Number pageSize) {
        return dictRepository.getAll(pageNumber, pageSize);
    }

    @Override
    public DictBO getById(Long id) {
        return dictRepository.getById(id);
    }

    @Override
    public List<DictBO> getByDictType(String dictType) {
        return dictRepository.getByDictType(dictType);
    }

    @Override
    public DictBO create(DictBO dictBO) {
        return dictRepository.create(dictBO);
    }

    @Override
    public DictBO update(DictBO dictBO) {
        return dictRepository.update(dictBO);
    }

    @Override
    public void deleteById(Long id) {
        dictRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        dictRepository.deleteByIds(ids);
    }
}
