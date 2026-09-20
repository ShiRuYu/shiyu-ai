package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 知识 文档 关系 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeDocumentRelationService {

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<DocumentSummary> listDocuments(ActorContext actor, Long pointId);

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @param documentIds 待处理的业务对象标识集合。
     */
    default void replaceDocuments(ActorContext actor, Long pointId, List<Long> documentIds) {
        replaceDocuments(actor, pointId, documentIds, "RELATED");
    }

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @param documentIds 待处理的业务对象标识集合。
     * @param relationType 用于完成本次业务处理的 relationType 参数。
     */
    void replaceDocuments(
            ActorContext actor, Long pointId, List<Long> documentIds, String relationType);

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> listPointIds(ActorContext actor, Long documentId);

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param pointIds 待处理的业务对象标识集合。
     */
    default void replacePoints(ActorContext actor, Long documentId, List<Long> pointIds) {
        replacePoints(actor, documentId, pointIds, "RELATED");
    }

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param pointIds 待处理的业务对象标识集合。
     * @param relationType 用于完成本次业务处理的 relationType 参数。
     */
    void replacePoints(
            ActorContext actor, Long documentId, List<Long> pointIds, String relationType);

    /**
     * 删除或移除 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     */
    void removeDocumentRelations(ActorContext actor, Long documentId);

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<DocumentRelationView> listDocumentRelations(ActorContext actor, Long documentId);

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param relations 用于完成本次业务处理的 relations 参数。
     */
    void replaceDocumentRelations(
            ActorContext actor, Long documentId, List<DocumentRelationRequest> relations);

    /**
     * 封装 文档 关系 相关的不可变数据及其字段约束。
     */
    record DocumentRelationRequest(Long documentId, String relationType) {}

    /**
     * 封装 文档 关系 View 相关的不可变数据及其字段约束。
     */
    record DocumentRelationView(
            Long id,
            Long sourceDocumentId,
            Long targetDocumentId,
            String relationType,
            String targetTitle) {}

    /**
     * 封装 文档 Summary 相关的不可变数据及其字段约束。
     */
    record DocumentSummary(
            Long id,
            Long spaceId,
            String title,
            String docType,
            String lifecycleStatus,
            String parseStatus) {}
}
