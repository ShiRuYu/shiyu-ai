package com.shiyu.ai.model.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import com.shiyu.ai.kernel.context.ActorContext;

public interface AiModelService {
    Pair<Long, List<AiModelResponse>> pageResponse(ActorContext actor, Long platformId, Number pageNo, Number pageSize);
    List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId);
    List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode);
    AiModelResponse detailResponse(ActorContext actor, Long id);
    AiModelResponse defaultResponse(ActorContext actor, Long platformId);
    AiModelResponse createResponse(ActorContext actor, AiModelRequest request);
    AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request);
    void deleteById(ActorContext actor, Long id);
    void deleteByIds(ActorContext actor, List<Long> ids);
    List<IdNameOptionVO> getOptions(ActorContext actor, Long platformId);
    AiModelResponse setDefaultResponse(ActorContext actor, Long id);
}
