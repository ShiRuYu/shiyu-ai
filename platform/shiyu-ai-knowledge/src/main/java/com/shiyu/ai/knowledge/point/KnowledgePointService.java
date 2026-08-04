package com.shiyu.ai.knowledge.point;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public interface KnowledgePointService {

    PageData<PointView> page(Long spaceId, int pageNum, int pageSize,
                             String keyword, String category);

    PointView get(Long pointId);

    KnowledgeResponse getResponse(Long pointId);

    KnowledgeGraphResponse graph(Long pointId);

    PointView create(Long spaceId, CreatePointRequest request);

    PointView update(Long pointId, UpdatePointRequest request);

    void delete(Long pointId);

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
