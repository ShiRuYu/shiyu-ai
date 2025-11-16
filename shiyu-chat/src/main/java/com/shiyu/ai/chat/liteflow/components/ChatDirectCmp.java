package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.model.ModelEnum;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;

@LiteflowComponent("CHAT_DIRECT")
public class ChatDirectCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        String result =  chatEngine.call(query, ModelEnum.LOCAL);
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), result);
    }
}
