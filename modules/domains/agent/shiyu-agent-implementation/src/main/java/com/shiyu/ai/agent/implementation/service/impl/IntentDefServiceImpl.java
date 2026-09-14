package com.shiyu.ai.agent.implementation.service.impl;

import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;
import com.shiyu.ai.agent.implementation.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.implementation.port.repository.IntentDefRepository;
import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.service.IntentDefService;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/** 意图定义服务实现层 */
@Slf4j
@Service
public class IntentDefServiceImpl implements IntentDefService {
    /**
     * {@code pageView} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param n 参数值，用于执行当前操作。
     * @param s 参数值，用于执行当前操作。
     * @param a 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param cat 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Pair<Long, List<IntentDefVO>> pageView(
            ActorContext actor,
            Number n,
            Number s,
            String a,
            String name,
            String code,
            String cat) {
        requireActor(actor);
        var p = getPageBO(actor, n, s, a, name, code, cat);
        return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), IntentDefVO.class));
    }

    /**
     * {@code detailView} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public IntentDefVO detailView(ActorContext actor, Long id) {
        requireActor(actor);
        return MapstructUtils.convert(getByIdBO(actor, id), IntentDefVO.class);
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public IntentDefVO create(ActorContext actor, IntentDefRequest r) {
        requireActor(actor);
        return MapstructUtils.convert(
                createBO(actor, MapstructUtils.convert(r, IntentDefBO.class)), IntentDefVO.class);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public IntentDefVO update(ActorContext actor, Long id, IntentDefRequest r) {
        requireActor(actor);
        IntentDefBO b = MapstructUtils.convert(r, IntentDefBO.class);
        b.setId(id);
        return MapstructUtils.convert(updateBO(actor, b), IntentDefVO.class);
    }

    @Resource private IntentDefRepository intentDefRepository;

    private Pair<Long, List<IntentDefBO>> getPageBO(
            ActorContext actor,
            Number pageNo,
            Number pageSize,
            String agentId,
            String name,
            String code,
            String category) {
        return intentDefRepository.selectPage(
                actor.tenantId(), pageNo, pageSize, agentId, name, code, category);
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

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteById(ActorContext actor, Long id) {
        requireActor(actor);
        IntentDefBO bo = intentDefRepository.selectById(actor.tenantId(), id);
        intentDefRepository.deleteById(actor.tenantId(), id);
        if (bo != null) {
            refreshFactory(actor);
        }
    }

    /**
     * {@code deleteByIds} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param ids 参数值，用于执行当前操作。
     */
    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        requireActor(actor);
        for (Long id : ids) {
            intentDefRepository.deleteById(actor.tenantId(), id);
        }
        refreshFactory(actor);
    }

    /**
     * {@code listAllOptions} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<IdNameOptionVO> listAllOptions(ActorContext actor) {
        requireActor(actor);
        return intentDefRepository.selectAllOptions(actor.tenantId());
    }

    private void refreshFactory(ActorContext actor) {
        try {
            List<IntentDefBO> all =
                    intentDefRepository.selectByAgentId(actor.tenantId(), "default");
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
