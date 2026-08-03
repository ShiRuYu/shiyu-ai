package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.port.repository.IntentDefRepository;
import com.shiyu.ai.agent.service.IntentDefService;
import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.agent.node.intent.IntentDefinitionFactory;
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
    @Override public Pair<Long, List<IntentDefVO>> pageView(Number n, Number s, String a, String name, String code, String cat) { var p=getPageBO(n,s,a,name,code,cat); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), IntentDefVO.class)); }
    @Override public IntentDefVO detailView(Long id) { return MapstructUtils.convert(getByIdBO(id), IntentDefVO.class); }
    @Override public IntentDefVO create(IntentDefRequest r) { return MapstructUtils.convert(createBO(MapstructUtils.convert(r, IntentDefBO.class)), IntentDefVO.class); }
    @Override public IntentDefVO update(Long id, IntentDefRequest r) { IntentDefBO b=MapstructUtils.convert(r, IntentDefBO.class); b.setId(id); return MapstructUtils.convert(updateBO(b), IntentDefVO.class); }

    @Resource
    private IntentDefRepository intentDefRepository;

    private Pair<Long, List<IntentDefBO>> getPageBO(Number pageNo, Number pageSize, String agentId, String name, String code, String category) {
        return intentDefRepository.selectPage(pageNo, pageSize, agentId, name, code, category);
    }

    private IntentDefBO getByIdBO(Long id) {
        return intentDefRepository.selectById(id);
    }

    private IntentDefBO createBO(IntentDefBO bo) {
        IntentDefBO result = intentDefRepository.create(bo);
        refreshFactory();
        return result;
    }

    private IntentDefBO updateBO(IntentDefBO bo) {
        IntentDefBO result = intentDefRepository.update(bo);
        refreshFactory();
        return result;
    }

    @Override
    public void deleteById(Long id) {
        IntentDefBO bo = intentDefRepository.selectById(id);
        intentDefRepository.deleteById(id);
        if (bo != null) {
            refreshFactory();
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            intentDefRepository.deleteById(id);
        }
        refreshFactory();
    }

    @Override
    public List<IdNameOptionVO> listAllOptions() {
        return intentDefRepository.selectAllOptions();
    }

    private void refreshFactory() {
        try {
            List<IntentDefBO> all = intentDefRepository.selectByAgentId("default");
            IntentDefinitionFactory.reloadFromDb(all);
            log.info("IntentDefinitionFactory 已刷新，共计 {} 条意图定义", all != null ? all.size() : 0);
        } catch (Exception e) {
            log.error("刷新 IntentDefinitionFactory 失败", e);
        }
    }
}
