package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LiteflowComponent("CHAT_DIRECT")
public class ChatDirectCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        
        log.info("执行直接对话模式：{}", query);
        
        // 直接调用模型回答问题
        String result = chatEngine.call(query, ModelEnum.SILICON_FLOW);
        
        log.info("直接对话完成");
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), result);
    }
}
