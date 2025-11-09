package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

/**
 * 根据意图选择策略（CoT / ToT / ReAct）
 */
@LiteflowComponent("STRATEGY_SELECT")
public class StrategySelectCmp extends NodeComponent {
    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String intentType = context.get(GlobalContext.ChatBizKeyEnum.INTENT_TYPE.getCode(), "defalut");
        String chainToCall = "chain.cot";
        if ("intentType".equals(intentType)) {
            chainToCall = "chain.tot";
        }
        context.set(GlobalContext.ChatBizKeyEnum.CHAIN.getCode(), chainToCall);
        context.set(GlobalContext.ChatBizKeyEnum.LAST_CHAIN.getCode(), chainToCall);

        System.out.println("STRATEGY_SELECT 节点执行完毕，chainToCall=" + chainToCall);
    }

    @Override
    public boolean isContinueOnError() { return true; }
}
