package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@LiteflowComponent("CHAT_TOT")
class ChatToTCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");

        List<String> thoughts = new ArrayList<>();

        // 生成 5 个模型响应作为候选方案
        for (int i = 1; i <= 5; i++) {
            String result = chatEngine.call(query + " [方案" + i + "]", ModelEnum.LOCAL);
            thoughts.add(result);
        }
        context.set(GlobalContext.ChatBizKeyEnum.TOT_THOUGHT_NODES.getCode(), thoughts);
        context.set(GlobalContext.ChatBizKeyEnum.TOT_FINAL_THOUGHT.getCode(), thoughts.getFirst());
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), thoughts.getFirst());
        System.out.println("MODEL_CALL_TOT 节点完成，生成 " + thoughts.size() + " 个方案");
    }

    @Override
    public boolean isContinueOnError() {
        return true;
    }
}
