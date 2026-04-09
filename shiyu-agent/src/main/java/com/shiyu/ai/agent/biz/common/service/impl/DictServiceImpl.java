package com.shiyu.ai.agent.biz.common.service.impl;

import com.shiyu.ai.agent.biz.common.repository.DictRepository;
import com.shiyu.ai.agent.biz.common.service.DictService;
import com.shiyu.ai.agent.domain.bo.DictBO;
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
    public Pair<Long, List<DictBO>> getAll(Number pageNo, Number pageSize) {
        return dictRepository.selectPage(pageNo, pageSize);
    }

    @Override
    public DictBO getById(Long id) {
        return dictRepository.selectById(id);
    }

    @Override
    public List<DictBO> getByDictType(String dictType) {
        return dictRepository.selectByDictType(dictType);
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
