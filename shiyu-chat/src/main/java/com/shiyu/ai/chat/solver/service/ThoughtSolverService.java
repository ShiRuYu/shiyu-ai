package com.shiyu.ai.chat.solver.service;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;

import java.util.Collections;
import java.util.List;

public class ThoughtSolverService {

    public ThoughtSolution solve(List<ThoughtNode> nodes) {

        if (nodes == null || nodes.isEmpty()) {
            ThoughtSolution emptySolution = new ThoughtSolution();
            emptySolution.setNodes(Collections.emptyList());
            emptySolution.setBestNode(null);
            return emptySolution;
        }

        // 1. 使用 SolverConfig 构建 SolverFactory
        SolverFactory<ThoughtSolution> factory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(ThoughtSolution.class)
                        .withEntityClasses(ThoughtNode.class)
                        .withScoreDirectorFactory(
                                new ScoreDirectorFactoryConfig()
                                        .withEasyScoreCalculatorClass(ThoughtScoreCalculator.class)
                        )
        );

        // 2. 构建 Solver
        Solver<ThoughtSolution> solver = factory.buildSolver();

        // 3. 构建初始解
        ThoughtSolution solution = new ThoughtSolution();
        solution.setNodes(nodes);

        // 4. 执行求解
        ThoughtSolution bestSolution = solver.solve(solution);

        return bestSolution;
    }
}

