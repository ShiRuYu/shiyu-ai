package com.shiyu.ai.chat.solver.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;

import java.util.Comparator;

public class ThoughtScoreCalculator implements EasyScoreCalculator<ThoughtSolution, HardSoftScore> {
    @Override
    public HardSoftScore calculateScore(ThoughtSolution solution) {
        double maxScore = solution.getNodes().stream().mapToDouble(ThoughtNode::getScore).max().orElse(0.0);
        solution.setBestNode(solution.getNodes().stream().max(Comparator.comparingDouble(ThoughtNode::getScore)).orElse(null));
        return HardSoftScore.ofSoft((int)(maxScore * 100));
    }
}
