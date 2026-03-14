package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 根据选择的策略调用对应子链
 */
@Slf4j
@LiteflowComponent("CHAIN_EXECUTE")
public class ChainExecuteCmp extends NodeComponent {
    @Resource
    private FlowExecutor flowExecutor;

    @Override
    public void process() {
        try {
            log.info("执行 CHAIN_EXECUTE 节点");
            GlobalContext context = this.getContextBean(GlobalContext.class);
            String chainToCall = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
            //调用子链（CoT/ToT/ReAct）
            this.invoke2Resp(chainToCall, context);
        } catch (Exception e) {
            log.error("CHAIN_EXECUTE 异常: " + e.getMessage());
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
