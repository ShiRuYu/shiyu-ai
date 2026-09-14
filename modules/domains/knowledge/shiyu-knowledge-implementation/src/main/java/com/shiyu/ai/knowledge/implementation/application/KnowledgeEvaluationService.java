package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.constraints.NotBlank;

/**
 * KnowledgeEvaluationService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeEvaluationService {

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    PageData<CaseView> page(ActorContext actor, int pageNum, int pageSize, Long spaceId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    CaseView create(ActorContext actor, CreateCaseRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void delete(ActorContext actor, Long id);

    /**
     * 执行当前接口定义的业务流程。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    RunResult run(ActorContext actor, RunRequest request);

    /**
     * {@code CaseView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param question question 属性，表示该记录组件承载的数据。
     * @param expectedDocIds expectedDocIds 属性，表示该记录组件承载的数据。
     * @param expectedAnswer expectedAnswer 属性，表示该记录组件承载的数据。
     */
    record CaseView(
            Long id, Long spaceId, String question, String expectedDocIds, String expectedAnswer) {}

    /**
     * {@code CreateCaseRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param question question 属性，表示该记录组件承载的数据。
     * @param expectedDocIds expectedDocIds 属性，表示该记录组件承载的数据。
     * @param expectedAnswer expectedAnswer 属性，表示该记录组件承载的数据。
     */
    record CreateCaseRequest(
            Long spaceId,
            @NotBlank String question,
            String expectedDocIds,
            String expectedAnswer) {}

    /**
     * {@code RunRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param topK topK 属性，表示该记录组件承载的数据。
     */
    record RunRequest(Long spaceId, Integer topK) {}

    /**
     * {@code RunResult} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param caseCount caseCount 属性，表示该记录组件承载的数据。
     * @param topK topK 属性，表示该记录组件承载的数据。
     * @param recallAtK recallAtK 属性，表示该记录组件承载的数据。
     * @param mrr mrr 属性，表示该记录组件承载的数据。
     * @param citationAccuracy citationAccuracy 属性，表示该记录组件承载的数据。
     * @param cases cases 属性，表示该记录组件承载的数据。
     */
    record RunResult(
            Long spaceId,
            int caseCount,
            int topK,
            double recallAtK,
            double mrr,
            double citationAccuracy,
            java.util.List<CaseResult> cases) {}

    /**
     * {@code CaseResult} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param caseId caseId 属性，表示该记录组件承载的数据。
     * @param question question 属性，表示该记录组件承载的数据。
     * @param recallAtK recallAtK 属性，表示该记录组件承载的数据。
     * @param reciprocalRank reciprocalRank 属性，表示该记录组件承载的数据。
     * @param citationAccuracy citationAccuracy 属性，表示该记录组件承载的数据。
     * @param expectedDocumentIds expectedDocumentIds 属性，表示该记录组件承载的数据。
     * @param returnedDocumentIds returnedDocumentIds 属性，表示该记录组件承载的数据。
     */
    record CaseResult(
            Long caseId,
            String question,
            double recallAtK,
            double reciprocalRank,
            double citationAccuracy,
            java.util.List<Long> expectedDocumentIds,
            java.util.List<Long> returnedDocumentIds) {}
}
