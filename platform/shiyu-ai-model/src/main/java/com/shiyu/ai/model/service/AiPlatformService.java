package com.shiyu.ai.model.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public interface AiPlatformService {
    Pair<Long, List<AiPlatformResponse>> pageResponse(Number pageNo, Number pageSize, String name, String code);
    List<AiPlatformResponse> enabledResponse();
    AiPlatformResponse detailResponse(Long id);
    AiPlatformResponse codeResponse(String code);
    AiPlatformResponse defaultResponse();
    AiPlatformResponse createResponse(AiPlatformRequest request);
    AiPlatformResponse updateResponse(Long id, AiPlatformRequest request);
    void deleteById(Long id);
    List<IdNameOptionVO> getOptions();
    AiPlatformResponse setDefaultResponse(Long id);
}
