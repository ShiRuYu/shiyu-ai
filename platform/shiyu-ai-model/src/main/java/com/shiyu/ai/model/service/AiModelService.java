package com.shiyu.ai.model.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public interface AiModelService {
    Pair<Long, List<AiModelResponse>> pageResponse(Long platformId, Number pageNo, Number pageSize);
    List<AiModelResponse> byPlatformResponse(Long platformId);
    List<AiModelResponse> byPlatformCodeResponse(String platformCode);
    AiModelResponse detailResponse(Long id);
    AiModelResponse defaultResponse(Long platformId);
    AiModelResponse createResponse(AiModelRequest request);
    AiModelResponse updateResponse(Long id, AiModelRequest request);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
    List<IdNameOptionVO> getOptions(Long platformId);
    AiModelResponse setDefaultResponse(Long id);
}
