package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.service.IntentService;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.node.Intent;
import com.shiyu.ai.common.core.utils.JsonUtils;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * 意图识别
 */
@LiteflowComponent("INTENT")
public class IntentCmp extends NodeComponent {

    @Resource
    private IntentService intentService;

    @Override
    public void process() {
        // 使用 this.getContextBean 获取全局上下文
        GlobalContext context = this.getContextBean(GlobalContext.class);
        List<Intent> intentList = intentService.list("test");
        Intent intent = intentService.detect(context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode()), intentList);

        context.set(GlobalContext.ChatBizKeyEnum.INTENT.getCode(), intent);
        context.set(GlobalContext.ChatBizKeyEnum.CHAIN.getCode(), intent.getChainToCall());

        System.out.println("INTENT 节点执行完毕，intent=" + JsonUtils.toJsonString(intent));
    }

    @Override
    public boolean isContinueOnError() { return true; }
}

