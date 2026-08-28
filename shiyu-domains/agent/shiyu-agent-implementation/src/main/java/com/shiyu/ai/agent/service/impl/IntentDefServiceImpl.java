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
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.core.exception.ServiceException;

/**
 * 意图定义服务实现层
 */
@Slf4j
@Service
public class IntentDefServiceImpl implements IntentDefService {
    @Override public Pair<Long, List<IntentDefVO>> pageView(ActorContext actor, Number n, Number s, String a, String name, String code, String cat) { requireActor(actor); var p=getPageBO(actor,n,s,a,name,code,cat); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), IntentDefVO.class)); }
    @Override public IntentDefVO detailView(ActorContext actor, Long id) { requireActor(actor); return MapstructUtils.convert(getByIdBO(actor,id), IntentDefVO.class); }
    @Override public IntentDefVO create(ActorContext actor, IntentDefRequest r) { requireActor(actor); return MapstructUtils.convert(createBO(actor,MapstructUtils.convert(r, IntentDefBO.class)), IntentDefVO.class); }
    @Override public IntentDefVO update(ActorContext actor, Long id, IntentDefRequest r) { requireActor(actor); IntentDefBO b=MapstructUtils.convert(r, IntentDefBO.class); b.setId(id); return MapstructUtils.convert(updateBO(actor,b), IntentDefVO.class); }

    @Resource
    private IntentDefRepository intentDefRepository;

    private Pair<Long, List<IntentDefBO>> getPageBO(ActorContext actor, Number pageNo, Number pageSize, String agentId, String name, String code, String category) {
        return intentDefRepository.selectPage(actor.tenantId(), pageNo, pageSize, agentId, name, code, category);
    }

    private IntentDefBO getByIdBO(ActorContext actor, Long id) {
        return intentDefRepository.selectById(actor.tenantId(), id);
    }

    private IntentDefBO createBO(ActorContext actor, IntentDefBO bo) {
        IntentDefBO result = intentDefRepository.create(actor.tenantId(), bo);
        refreshFactory(actor);
        return result;
    }

    private IntentDefBO updateBO(ActorContext actor, IntentDefBO bo) {
        IntentDefBO result = intentDefRepository.update(actor.tenantId(), bo);
        refreshFactory(actor);
        return result;
    }

    @Override
    public void deleteById(ActorContext actor, Long id) {
        requireActor(actor);
        IntentDefBO bo = intentDefRepository.selectById(actor.tenantId(), id);
        intentDefRepository.deleteById(actor.tenantId(), id);
        if (bo != null) {
            refreshFactory(actor);
        }
    }

    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        requireActor(actor);
        for (Long id : ids) {
            intentDefRepository.deleteById(actor.tenantId(), id);
        }
        refreshFactory(actor);
    }

    @Override
    public List<IdNameOptionVO> listAllOptions(ActorContext actor) {
        requireActor(actor);
        return intentDefRepository.selectAllOptions(actor.tenantId());
    }

    private void refreshFactory(ActorContext actor) {
        try {
            List<IntentDefBO> all = intentDefRepository.selectByAgentId(actor.tenantId(), "default");
            IntentDefinitionFactory.reloadFromDb(all);
            log.info("IntentDefinitionFactory 已刷新，共计 {} 条意图定义", all != null ? all.size() : 0);
        } catch (Exception e) {
            log.error("刷新 IntentDefinitionFactory 失败", e);
        }
    }

    private void requireActor(ActorContext actor) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
    }
}
