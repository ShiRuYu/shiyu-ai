package com.shiyu.ai.model.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.domain.model.AiModelBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AiModelRepository {
    Pair<Long, List<AiModelBO>> selectPage(Long platformId, Number pageNo, Number pageSize);
    List<AiModelBO> selectByPlatformId(Long platformId);
    AiModelBO selectById(Long id);
    AiModelBO selectDefaultByPlatformId(Long platformId);
    AiModelBO create(AiModelBO bo);
    AiModelBO update(AiModelBO bo);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
    List<IdNameOptionVO> selectOptions(Long platformId);
    void clearDefaultExcept(Long platformId, Long excludeId);
}
