package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** 知识点与企业文档的统一关系服务。 */
public interface KnowledgeDocumentRelationService {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<DocumentSummary> listDocuments(ActorContext actor, Long pointId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     * @param documentIds 方法参数。
     */
    default void replaceDocuments(ActorContext actor, Long pointId, List<Long> documentIds) {
        replaceDocuments(actor, pointId, documentIds, "RELATED");
    }

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     * @param documentIds 方法参数。
     * @param relationType 方法参数。
     */
    void replaceDocuments(
            ActorContext actor, Long pointId, List<Long> documentIds, String relationType);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> listPointIds(ActorContext actor, Long documentId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param pointIds 方法参数。
     */
    default void replacePoints(ActorContext actor, Long documentId, List<Long> pointIds) {
        replacePoints(actor, documentId, pointIds, "RELATED");
    }

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param pointIds 方法参数。
     * @param relationType 方法参数。
     */
    void replacePoints(
            ActorContext actor, Long documentId, List<Long> pointIds, String relationType);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     */
    void removeDocumentRelations(ActorContext actor, Long documentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<DocumentRelationView> listDocumentRelations(ActorContext actor, Long documentId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param relations 方法参数。
     */
    void replaceDocumentRelations(
            ActorContext actor, Long documentId, List<DocumentRelationRequest> relations);

    /**
     * {@code DocumentRelationRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param documentId documentId 属性，表示该记录组件承载的数据。
     * @param relationType relationType 属性，表示该记录组件承载的数据。
     */
    record DocumentRelationRequest(Long documentId, String relationType) {}

    /**
     * {@code DocumentRelationView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param sourceDocumentId sourceDocumentId 属性，表示该记录组件承载的数据。
     * @param targetDocumentId targetDocumentId 属性，表示该记录组件承载的数据。
     * @param relationType relationType 属性，表示该记录组件承载的数据。
     * @param targetTitle targetTitle 属性，表示该记录组件承载的数据。
     */
    record DocumentRelationView(
            Long id,
            Long sourceDocumentId,
            Long targetDocumentId,
            String relationType,
            String targetTitle) {}

    /**
     * {@code DocumentSummary} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     * @param docType docType 属性，表示该记录组件承载的数据。
     * @param lifecycleStatus lifecycleStatus 属性，表示该记录组件承载的数据。
     * @param parseStatus parseStatus 属性，表示该记录组件承载的数据。
     */
    record DocumentSummary(
            Long id,
            Long spaceId,
            String title,
            String docType,
            String lifecycleStatus,
            String parseStatus) {}
}
