package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.repository.IntentDefRepository;
import com.shiyu.ai.agent.biz.agent.service.IntentDefService;
import com.shiyu.ai.agent.domain.bo.IntentDefBO;
import com.shiyu.ai.agent.domain.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 意图定义服务实现层
 */
@Slf4j
@Service
public class IntentDefServiceImpl implements IntentDefService {

    @Resource
    private IntentDefRepository intentDefRepository;

    @Override
    public Pair<Long, List<IntentDefBO>> getPage(Number pageNo, Number pageSize, String agentId, String category) {
        return intentDefRepository.selectPage(pageNo, pageSize, agentId, category);
    }

    @Override
    public IntentDefBO getById(Long id) {
        return intentDefRepository.selectById(id);
    }

    @Override
    public IntentDefBO create(IntentDefBO bo) {
        return intentDefRepository.create(bo);
    }

    @Override
    public IntentDefBO update(IntentDefBO bo) {
        return intentDefRepository.update(bo);
    }

    @Override
    public void deleteById(Long id) {
        intentDefRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        intentDefRepository.deleteByIds(ids);
    }

    @Override
    public List<IdNameOptionVO> listAllOptions() {
        return intentDefRepository.selectAllOptions();
    }
}
