package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;

/**
 * 根据选择的策略调用对应子链
 */
@LiteflowComponent("CHAIN_EXECUTE")
class ChainExecuteCmp extends NodeComponent {
    @Resource
    private FlowExecutor flowExecutor;

    @Override
    public void process() {
        try {
            System.out.println("执行 CHAIN_EXECUTE 节点");
            GlobalContext context = this.getContextBean(GlobalContext.class);
            String chainToCall = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
            //调用子链（CoT/ToT/ReAct）
            this.invoke2Resp(chainToCall, context);
        } catch (Exception e) {
            System.err.println("CHAIN_EXECUTE 异常: " + e.getMessage());
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
