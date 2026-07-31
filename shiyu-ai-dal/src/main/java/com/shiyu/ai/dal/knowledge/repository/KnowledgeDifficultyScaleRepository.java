package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDifficultyScaleDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDifficultyScaleLevelDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDifficultyScaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeDifficultyScaleRepository {

    private final KnowledgeDifficultyScaleMapper scaleMapper;
    private final KnowledgeDifficultyScaleLevelMapper levelMapper;

    public KnowledgeDifficultyScaleDO findScale(Long scaleId) {
        return scaleMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleDO::getId, scaleId)
                .eq(KnowledgeDifficultyScaleDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleDO::getStatus, 1));
    }

    public List<KnowledgeDifficultyScaleLevelDO> findLevels(Long scaleId) {
        return levelMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleLevelDO::getScaleId, scaleId)
                .eq(KnowledgeDifficultyScaleLevelDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleLevelDO::getStatus, 1)
                .orderBy(KnowledgeDifficultyScaleLevelDO::getLevel, true));
    }
}
