package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;
import com.shiyu.ai.chat.solver.service.ThoughtSolverService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

import java.util.List;

@LiteflowComponent("TOT_SOLVE")
class ChatToTSolveCmp extends NodeComponent {
    @Override
    public void process() {
        try {
            System.out.println("执行 TOT_SOLVE 节点");
            GlobalContext context = this.getContextBean(GlobalContext.class);
            List<ThoughtNode> nodes = context.get(GlobalContext.ChatBizKeyEnum.TOT_THOUGHT_NODES.getCode());
            ThoughtSolverService solverService = new ThoughtSolverService();
            ThoughtSolution best = solverService.solve(nodes);
            context.set(GlobalContext.ChatBizKeyEnum.TOT_FINAL_THOUGHT.getCode(), best.getBestNode());
            context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), best.getBestNode().getContent());
            System.out.println("最佳方案: " + best.getBestNode().getContent() + ", score=" + best.getBestNode().getScore());
        } catch (Exception e) {
            System.err.println("TOT_SOLVE 异常: " + e.getMessage());
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
