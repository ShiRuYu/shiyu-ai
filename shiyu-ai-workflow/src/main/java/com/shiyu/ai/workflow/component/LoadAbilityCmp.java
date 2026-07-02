package com.shiyu.ai.workflow.component;

import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.education.repository.AbilityRepository;
import com.shiyu.ai.workflow.context.AbilityContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Component("loadAbilityCmp")
@RequiredArgsConstructor
public class LoadAbilityCmp extends NodeComponent {
    private final AbilityRepository abilityRepository;

    @Override
    public void process() throws Exception {
        AbilityContext ctx = this.getContextBean(AbilityContext.class);
        List<AbilityDO> all = abilityRepository.selectByStudent(ctx.getStudentId());
        Map<String, Double> avg = new LinkedHashMap<>();
        avg.put("remember", 0.0); avg.put("understand", 0.0); avg.put("apply", 0.0);
        avg.put("analyze", 0.0); avg.put("evaluate", 0.0); avg.put("create", 0.0);
        double totalMastery = 0;
        if (!all.isEmpty()) {
            for (AbilityDO a : all) {
                if (a.getRemember() != null) avg.put("remember", avg.get("remember") + a.getRemember());
                if (a.getUnderstand() != null) avg.put("understand", avg.get("understand") + a.getUnderstand());
                if (a.getApply() != null) avg.put("apply", avg.get("apply") + a.getApply());
                if (a.getAnalyze() != null) avg.put("analyze", avg.get("analyze") + a.getAnalyze());
                if (a.getEvaluate() != null) avg.put("evaluate", avg.get("evaluate") + a.getEvaluate());
                if (a.getCreateScore() != null) avg.put("create", avg.get("create") + a.getCreateScore());
                if (a.getOverallMastery() != null) totalMastery += a.getOverallMastery();
            }
            int n = all.size();
            for (String k : avg.keySet()) avg.put(k, avg.get(k) / n);
            totalMastery /= n;
        }
        ctx.setAbilities(avg);
        ctx.setOverallMastery(Math.round(totalMastery * 10.0) / 10.0);
        log.info("能力评估完成: overall={}%", ctx.getOverallMastery());
    }
}