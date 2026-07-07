package com.shiyu.ai.aiagent.workflow.component;

import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.dal.repository.education.AbilityRepository;
import com.shiyu.ai.aiagent.workflow.context.RecommendContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component("loadWeakPointsCmp")
@RequiredArgsConstructor
public class LoadWeakPointsCmp extends NodeComponent {
    private final AbilityRepository abilityRepository;

    @Override
    public void process() throws Exception {
        RecommendContext ctx = this.getContextBean(RecommendContext.class);
        List<AbilityDO> abilities = abilityRepository.selectByStudent(ctx.getStudentId());
        List<Long> weakIds = abilities.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(AbilityDO::getKnowledgeId).toList();
        ctx.setWeakKnowledgeIds(weakIds);
        log.info("薄弱知识点: {} 个", weakIds.size());
    }
}