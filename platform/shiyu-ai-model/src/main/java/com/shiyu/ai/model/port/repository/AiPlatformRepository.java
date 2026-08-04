package com.shiyu.ai.model.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AiPlatformRepository {
    Pair<Long, List<AiPlatformBO>> selectPage(Number pageNo, Number pageSize, String name, String code);
    List<AiPlatformBO> selectAllEnabled();
    AiPlatformBO selectById(Long id);
    AiPlatformBO selectByCode(String code);
    AiPlatformBO selectDefault();
    AiPlatformBO create(AiPlatformBO bo);
    AiPlatformBO update(AiPlatformBO bo);
    void deleteById(Long id);
    List<IdNameOptionVO> selectOptions();
    void clearDefaultExcept(Long excludeId);
}
