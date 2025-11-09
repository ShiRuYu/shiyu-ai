package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

/**
 * 意图识别
 */
@LiteflowComponent("INTENT")
public class IntentCmp extends NodeComponent {
    @Override
    public void process() {
        String intent = "planning_task";
        String intentType = "intentType";

        // 使用 this.getContextBean 获取全局上下文
        GlobalContext context = this.getContextBean(GlobalContext.class);
        context.set(GlobalContext.ChatBizKeyEnum.INTENT.getCode(), intent);
        context.set(GlobalContext.ChatBizKeyEnum.INTENT_TYPE.getCode(), intentType);
        context.set(GlobalContext.ChatBizKeyEnum.LAST_INTENT.getCode(), intent);

        System.out.println("INTENT 节点执行完毕，intent=" + intent+" , intentType=" + intentType);
    }

    @Override
    public boolean isContinueOnError() { return true; }
}

