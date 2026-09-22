package com.shiyu.ai.agent.implementation.service.impl;

import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;
import com.shiyu.ai.agent.implementation.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.implementation.port.repository.IntentDefRepository;
import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.service.IntentDefService;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提供 Intent Def 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class IntentDefServiceImpl implements IntentDefService {
    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param n 用于完成本次业务处理的 n 参数。
     * @param s 用于完成本次业务处理的 s 参数。
     * @param a 用于完成本次业务处理的 a 参数。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param cat 用于完成本次业务处理的 cat 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
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
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    @Override
    public IntentDefVO detailView(ActorContext actor, Long id) {
        requireActor(actor);
        return MapstructUtils.convert(getByIdBO(actor, id), IntentDefVO.class);
    }

    /**
     * 创建或保存 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param r 用于完成本次业务处理的 r 参数。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    @Override
    public IntentDefVO create(ActorContext actor, IntentDefRequest r) {
        requireActor(actor);
        return MapstructUtils.convert(
                createBO(actor, MapstructUtils.convert(r, IntentDefBO.class)), IntentDefVO.class);
    }

    /**
     * 更新或设置 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param r 用于完成本次业务处理的 r 参数。
     * @return 返回 Intent Def 相关操作生成的结果数据。
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
     * 删除或移除 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
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
     * 删除或移除 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param ids 待处理的业务对象标识集合。
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
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
