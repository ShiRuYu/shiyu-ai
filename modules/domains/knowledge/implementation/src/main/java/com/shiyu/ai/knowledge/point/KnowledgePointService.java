package com.shiyu.ai.knowledge.point;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public interface KnowledgePointService {

    PageData<PointView> page(ActorContext actor, Long spaceId, int pageNum, int pageSize,
                             String keyword, String category);

    PointView get(ActorContext actor, Long pointId);

    KnowledgeResponse getResponse(ActorContext actor, Long pointId);

    KnowledgeGraphResponse graph(ActorContext actor, Long pointId);

    PointView create(ActorContext actor, Long spaceId, CreatePointRequest request);

    PointView update(ActorContext actor, Long pointId, UpdatePointRequest request);

    void delete(ActorContext actor, Long pointId);

    record PointView(Long id, Long spaceId, String code, String name,
                     String description, Integer difficultyLevel,
                     String category, String tags) {
    }

    record CreatePointRequest(@NotBlank String code, @NotBlank String name,
                              String description,
                              @Min(1) @Max(100) Integer difficultyLevel,
                              String category, String tags) {
    }

    record UpdatePointRequest(String name, String description,
                              @Min(1) @Max(100) Integer difficultyLevel,
                              String category, String tags) {
    }
}
