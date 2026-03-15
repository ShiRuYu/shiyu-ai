package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 根据选择的策略调用对应子链（支持同步和流式）
 * 复用现有的 Direct/CoT/ToT 子链
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
                
            // 获取意图识别结果和要调用的子链名称
            String chainToCall = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
                
            if (chainToCall == null || chainToCall.trim().isEmpty()) {
                log.warn("未获取到子链名称，使用默认直接对话模式");
                chainToCall = "chatDirect";
            }
                
            log.info("意图识别完成，调用子链：{}", chainToCall);
                
            // 调用对应的子链（Direct/CoT/ToT）
            this.invoke2Resp(chainToCall, context);
                
            log.info("子链 {} 执行成功", chainToCall);
                
        } catch (Exception e) {
            log.error("CHAIN_EXECUTE 异常：{}", e.getMessage(), e);
            throw new RuntimeException("子链执行失败：" + e.getMessage(), e);
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
