package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.ChapterService;
import com.shiyu.ai.education.implementation.domain.model.ChapterBO;
import com.shiyu.ai.education.implementation.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ChapterRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.KnowledgeTextbookRepository;
import com.shiyu.ai.education.implementation.web.dto.ChapterResponse;
import com.shiyu.ai.education.implementation.web.request.ChapterRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 提供 章节 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("lambda")
public class ChapterServiceImpl implements ChapterService {

    /**
     * 章节仓储，表示当前对象中的对应属性。
     */
    private final ChapterRepository chapterRepository;
    /**
     * knowledgeTextbookRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeTextbookRepository knowledgeTextbookRepository;

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 章节 相关操作生成的结果数据。
     */
    @Override
    public ChapterResponse getById(ActorContext actor, Long id) {
        ChapterBO bo = chapterRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, ChapterResponse.class);
    }

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ChapterResponse> listByTextbookId(ActorContext actor, Long textbookId) {
        List<ChapterBO> boList =
                chapterRepository.selectByTextbookId(requireActor(actor).tenantId(), textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ChapterResponse> listRootChapters(ActorContext actor, Long textbookId) {
        List<ChapterBO> boList =
                chapterRepository.selectRootChapters(requireActor(actor).tenantId(), textbookId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param parentId 用于定位parent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ChapterResponse> listByParentId(ActorContext actor, Long parentId) {
        List<ChapterBO> boList =
                chapterRepository.selectByParentId(requireActor(actor).tenantId(), parentId);
        return MapstructUtils.convert(boList, ChapterResponse.class);
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse create(ActorContext actor, ChapterRequest request) {
        actor = requireActor(actor);
        ChapterBO chapter = new ChapterBO();
        applyRequest(chapter, request);
        chapterRepository.insert(actor.tenantId(), chapter);
        return MapstructUtils.convert(chapter, ChapterResponse.class);
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, Long id, ChapterRequest request) {
        actor = requireActor(actor);
        ChapterBO chapter = chapterRepository.selectById(actor.tenantId(), id);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + id);
        }
        applyRequest(chapter, request);
        chapterRepository.update(actor.tenantId(), chapter);
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long id) {
        actor = requireActor(actor);
        if (!chapterRepository.selectByParentId(actor.tenantId(), id).isEmpty()) {
            throw new ServiceException("请先删除子章节");
        }
        knowledgeTextbookRepository.deleteByChapterId(actor.tenantId(), id);
        chapterRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param chapterId 用于定位chapter的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> listKnowledgeIds(ActorContext actor, Long chapterId) {
        return knowledgeTextbookRepository
                .selectByChapterId(requireActor(actor).tenantId(), chapterId)
                .stream()
                .map(KnowledgeTextbookBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceKnowledgeIds(ActorContext actor, Long chapterId, List<Long> knowledgeIds) {
        actor = requireActor(actor);
        ChapterBO chapter = chapterRepository.selectById(actor.tenantId(), chapterId);
        if (chapter == null) {
            throw new ServiceException("章节不存在: " + chapterId);
        }
        knowledgeTextbookRepository.deleteByChapterId(actor.tenantId(), chapterId);
        if (knowledgeIds == null) {
            return;
        }
        java.util.Set<Long> distinctKnowledgeIds = new java.util.LinkedHashSet<>();
        for (Long knowledgeId : knowledgeIds) {
            if (knowledgeId != null) {
                distinctKnowledgeIds.add(knowledgeId);
            }
        }
        for (Long knowledgeId : distinctKnowledgeIds) {
            KnowledgeTextbookBO relation = new KnowledgeTextbookBO();
            relation.setChapterId(chapterId);
            relation.setTextbookId(chapter.getTextbookId());
            relation.setKnowledgeId(knowledgeId);
            relation.setStatus(1);
            knowledgeTextbookRepository.insert(actor.tenantId(), relation);
        }
    }

    private static void applyRequest(ChapterBO chapter, ChapterRequest request) {
        chapter.setTextbookId(request.getTextbookId());
        chapter.setName(request.getName());
        chapter.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        chapter.setChapterOrder(request.getChapterOrder() == null ? 0 : request.getChapterOrder());
        chapter.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private static ActorContext requireActor(ActorContext actor) {
        return Objects.requireNonNull(actor, "actor is required");
    }
}
