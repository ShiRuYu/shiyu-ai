package com.shiyu.ai.iam.implementation.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.iam.implementation.domain.model.DictBO;
import com.shiyu.ai.iam.implementation.port.repository.DictRepository;
import com.shiyu.ai.iam.implementation.request.DictRequest;
import com.shiyu.ai.iam.implementation.service.DictService;
import com.shiyu.ai.iam.implementation.service.convert.DictConverter;
import com.shiyu.ai.iam.implementation.vo.DictVO;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 提供 Dict 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class DictServiceImpl implements DictService {
    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param n 用于完成本次业务处理的 n 参数。
     * @param s 用于完成本次业务处理的 s 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    @Override
    public Pair<Long, List<DictVO>> pageView(ActorContext actor, Number n, Number s) {
        actor = requireActor(actor);
        var p = getAll(actor, n, s);
        return Pair.of(p.getLeft(), DictConverter.INSTANCE.toVOList(p.getRight()));
    }

    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<DictVO> byTypeView(ActorContext actor, String type) {
        actor = requireActor(actor);
        return DictConverter.INSTANCE.toVOList(getByDictType(actor, type));
    }

    /**
     * 创建或保存 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param r 用于完成本次业务处理的 r 参数。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    @Override
    public DictVO create(ActorContext actor, DictRequest r) {
        return DictConverter.INSTANCE.toVO(create(requireActor(actor), toBO(r)));
    }

    /**
     * 更新或设置 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param r 用于完成本次业务处理的 r 参数。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    @Override
    public DictVO update(ActorContext actor, Long id, DictRequest r) {
        DictBO b = toBO(r);
        b.setId(id);
        return DictConverter.INSTANCE.toVO(update(requireActor(actor), b));
    }

    private DictBO toBO(DictRequest r) {
        DictBO b = new DictBO();
        b.setDictType(r.getDictType());
        b.setDictLabel(r.getDictLabel());
        b.setDictValue(r.getDictValue());
        b.setDictSort(r.getDictSort());
        b.setCssClass(r.getCssClass());
        b.setListClass(r.getListClass());
        b.setIsDefault(r.getIsDefault());
        b.setRemark(r.getRemark());
        b.setStatus(r.getStatus());
        return b;
    }

    /**
     * dictRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final DictRepository dictRepository;

    /**
     * dictTypeCache 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Cache<String, List<DictBO>> dictTypeCache;

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dictRepository 用于完成本次业务处理的 dictRepository 参数。
     */
    public DictServiceImpl(DictRepository dictRepository) {
        this.dictRepository = dictRepository;
        this.dictTypeCache =
                Caffeine.newBuilder()
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

    /**
     * 删除或移除 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void deleteById(ActorContext actor, Long id) {
        actor = requireActor(actor);
        DictBO existing = dictRepository.selectById(actor.tenantId(), id);
        if (existing != null) {
            dictRepository.deleteById(actor.tenantId(), id);
            dictTypeCache.invalidate(cacheKey(actor, existing.getDictType()));
        }
    }

    /**
     * 删除或移除 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param ids 待处理的业务对象标识集合。
     */
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
