package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.constraints.NotBlank;

/**
 * 提供 知识 Evaluation 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeEvaluationService {

    /**
     * 查询 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
     */
    PageData<CaseView> page(ActorContext actor, int pageNum, int pageSize, Long spaceId);

    /**
     * 创建或保存 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
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
     * 执行 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
     */
    RunResult run(ActorContext actor, RunRequest request);

    /**
     * 封装 Case View 相关的不可变数据及其字段约束。
     */
    record CaseView(
            Long id, Long spaceId, String question, String expectedDocIds, String expectedAnswer) {}

    /**
     * 封装 Create Case 相关的不可变数据及其字段约束。
     */
    record CreateCaseRequest(
            Long spaceId,
            @NotBlank String question,
            String expectedDocIds,
            String expectedAnswer) {}

    /**
     * 封装 运行 相关的不可变数据及其字段约束。
     */
    record RunRequest(Long spaceId, Integer topK) {}

    /**
     * 封装 运行 相关的不可变数据及其字段约束。
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
     * 封装 Case 相关的不可变数据及其字段约束。
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
