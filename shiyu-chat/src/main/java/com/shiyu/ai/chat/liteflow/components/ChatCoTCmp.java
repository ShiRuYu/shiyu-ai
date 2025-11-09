package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.api.service.ChatEngine;
import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;

@LiteflowComponent("CHAT_COT")
public class ChatCoTCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        String result =  chatEngine.generateResponse(query);
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), result);
    }
}
