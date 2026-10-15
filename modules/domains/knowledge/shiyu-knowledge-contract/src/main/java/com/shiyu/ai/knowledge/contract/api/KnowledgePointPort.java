package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import java.util.List;

/**
 * 定义 知识 Point 领域与外部能力交互的端口契约。
 */
public interface KnowledgePointPort {

    /**
     * 查询指定知识点的知识响应。
     *
     * @param actor 当前操作主体上下文
     * @param pointId 知识点 ID
     * @return 知识点响应
     */
    KnowledgeResponse getResponse(ActorContext actor, Long pointId);

    /** 列出租户可见教育空间中的有效知识点，供学生辅导使用。 */
    List<KnowledgeResponse> listStudentEducationPoints(ActorContext actor);

    /** 读取一个租户可见的教育知识点，并拒绝私有或其他租户的知识点。 */
    KnowledgeResponse getStudentEducationPoint(ActorContext actor, Long pointId);
}
