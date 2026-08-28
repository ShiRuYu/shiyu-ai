package com.shiyu.ai.model.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import com.shiyu.ai.kernel.context.ActorContext;

public interface AiPlatformService {
    Pair<Long, List<AiPlatformResponse>> pageResponse(ActorContext actor, Number pageNo, Number pageSize, String name, String code);
    List<AiPlatformResponse> enabledResponse(ActorContext actor);
    AiPlatformResponse detailResponse(ActorContext actor, Long id);
    AiPlatformResponse codeResponse(ActorContext actor, String code);
    AiPlatformResponse defaultResponse(ActorContext actor);
    AiPlatformResponse createResponse(ActorContext actor, AiPlatformRequest request);
    AiPlatformResponse updateResponse(ActorContext actor, Long id, AiPlatformRequest request);
    void deleteById(ActorContext actor, Long id);
    List<IdNameOptionVO> getOptions(ActorContext actor);
    AiPlatformResponse setDefaultResponse(ActorContext actor, Long id);
}
