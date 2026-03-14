package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.service.IntentService;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.node.Intent;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 意图识别
 */
@Slf4j
@LiteflowComponent("INTENT")
public class IntentCmp extends NodeComponent {

    @Resource
    private IntentService intentService;

    @Override
    public void process() {
        // 使用 this.getContextBean 获取全局上下文
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode());
        
        List<Intent> intentList = intentService.list("default");
        
        // Step 1: 尝试关键词快速匹配
        Intent matchedIntent = matchByKeywords(query, intentList);
        
        if (matchedIntent == null) {
            // Step 2: 如果没有关键词匹配，使用大模型识别
            log.info("关键词匹配失败，使用大模型进行意图识别");
            matchedIntent = intentService.detect(query, intentList);
        } else {
            log.info("通过关键词匹配到意图：{}", matchedIntent.getName());
        }

        context.set(GlobalContext.ChatBizKeyEnum.INTENT.getCode(), matchedIntent);
        context.set(GlobalContext.ChatBizKeyEnum.CHAIN.getCode(), matchedIntent.getChainToCall());

        log.info("INTENT 节点执行完毕，intent=" + JSONUtils.toJsonString(matchedIntent));
    }

    /**
     * 通过关键词快速匹配意图
     */
    private Intent matchByKeywords(String text, List<Intent> intents) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase();
        
        for (Intent intent : intents) {
            // 从配置中读取的关键词可能在 content 或 name 中
            // 这里做一个简单的关键词匹配
            if ((intent.getName() != null && lowerText.contains(intent.getName().toLowerCase())) ||
                (intent.getContent() != null && lowerText.contains(intent.getContent().toLowerCase()))) {
                return intent;
            }
        }
        
        return null;
    }

    @Override
    public boolean isContinueOnError() { return true; }
}

