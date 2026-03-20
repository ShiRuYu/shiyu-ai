package com.shiyu.ai.chat.service.impl;

import com.shiyu.ai.chat.config.IntentConfig;
import com.shiyu.ai.chat.domain.node.Intent;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.service.IntentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class IntentServiceImpl implements IntentService {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private IntentConfig intentConfig;

    /**
     * 预定义的意图分类配置
     * 实际生产中可以从配置文件或数据库读取
     */
    private static final Map<String, List<Intent>> INTENT_CACHE = new HashMap<>();

    static {
        // 初始化测试分类的意图列表
        List<Intent> testIntents = new ArrayList<>();

        // 直接回答类意图
        Intent directIntent = new Intent();
        directIntent.setId("direct_greeting");
        directIntent.setName("问候寒暄");
        directIntent.setType("DIRECT");
        directIntent.setContent("用于处理日常问候、寒暄等简单对话");
        directIntent.setChainToCall("chatDirect");
        testIntents.add(directIntent);

        // 逻辑推理类意图
        Intent reasoningIntent = new Intent();
        reasoningIntent.setId("cot_reasoning");
        reasoningIntent.setName("逻辑推理");
        reasoningIntent.setType("COT");
        reasoningIntent.setContent("需要逐步推理的问题，如数学题、逻辑题等");
        reasoningIntent.setChainToCall("chatCoT");
        testIntents.add(reasoningIntent);

        // 多方案决策类意图
        Intent decisionIntent = new Intent();
        decisionIntent.setId("tot_decision");
        decisionIntent.setName("多方案决策");
        decisionIntent.setType("TOT");
        decisionIntent.setContent("需要从多个角度分析并提供多种解决方案的问题");
        decisionIntent.setChainToCall("chatToT");
        testIntents.add(decisionIntent);

        INTENT_CACHE.put("test", testIntents);
    }

    @Override
    public List<Intent> list(String category) {
        // 优先从配置中加载，如果没有则使用缓存的测试数据
        if (intentConfig != null && intentConfig.getCategories() != null) {
            List<IntentConfig.IntentDefinition> definitions = 
                    intentConfig.getCategories().get(category);
            if (definitions != null) {
                return definitions.stream()
                        .map(IntentConfig.IntentDefinition::toIntent)
                        .toList();
            }
        }
        return INTENT_CACHE.getOrDefault(category, new ArrayList<>());
    }

    @Override
    public Intent detect(String query, List<Intent> intents) {
        // 使用配置中的默认平台和模型
        return detect(query, intents, intentConfig.getPlatform(), intentConfig.getModel());
    }

    @Override
    public Intent detect(String query, List<Intent> intents, String platform, String modelName) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("输入文本为空，无法进行意图识别");
            return getDefaultIntent(intents);
        }

        // 构建意图识别的 Prompt
        String prompt = buildIntentDetectionPrompt(query, intents);

        try {
            // 调用大模型进行意图识别，使用传入的平台和模型参数
            ChatResult result = chatEngine.call(new LmRequest(prompt, platform, modelName, "IntentService"));
            log.info("意图识别结果：{}", result.getAnswer());

            // 解析模型返回结果
            Intent matchedIntent = parseIntentResult(result.getAnswer(), intents);
            if (matchedIntent != null) {
                return matchedIntent;
            }
        } catch (Exception e) {
            log.error("意图识别失败：{}", e.getMessage(), e);
        }

        // 如果识别失败，返回默认意图
        return getDefaultIntent(intents);
    }

    /**
     * 构建意图识别的 Prompt
     */
    private String buildIntentDetectionPrompt(String query, List<Intent> intents) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个意图识别助手。请分析用户输入的意图，并从以下选项中选择最匹配的一个：\n\n");

        for (int i = 0; i < intents.size(); i++) {
            Intent intent = intents.get(i);
            sb.append(String.format("%d. 【%s】-%s (类型：%s, 描述：%s)\n",
                    i + 1,
                    intent.getName(),
                    intent.getId(),
                    intent.getType(),
                    intent.getContent()));
        }

        sb.append("\n用户输入：").append(query).append("\n\n");
        sb.append("请只返回最匹配的意图 ID，不要输出其他内容。");

        return sb.toString();
    }

    /**
     * 解析模型返回的意图结果
     */
    private Intent parseIntentResult(String result, List<Intent> intents) {
        if (result == null || result.trim().isEmpty()) {
            return null;
        }

        // 尝试从结果中提取意图 ID
        String extractedId = result.trim();

        // 如果返回的是数字，可能是序号
        try {
            int index = Integer.parseInt(extractedId) - 1;
            if (index >= 0 && index < intents.size()) {
                return intents.get(index);
            }
        } catch (NumberFormatException e) {
            // 不是数字，尝试匹配 ID
            for (Intent intent : intents) {
                if (extractedId.equalsIgnoreCase(intent.getId()) ||
                        extractedId.contains(intent.getId())) {
                    return intent;
                }
            }
        }

        // 尝试通过关键词匹配
        String lowerResult = extractedId.toLowerCase();
        for (Intent intent : intents) {
            if (lowerResult.contains(intent.getName().toLowerCase()) ||
                    lowerResult.contains(intent.getType().toLowerCase())) {
                return intent;
            }
        }

        return null;
    }

    /**
     * 获取默认意图
     */
    private Intent getDefaultIntent(List<Intent> intents) {
        if (intents == null || intents.isEmpty()) {
            Intent defaultIntent = new Intent();
            defaultIntent.setId("default");
            defaultIntent.setName("默认");
            defaultIntent.setType("DIRECT");
            defaultIntent.setContent("默认意图，直接回答问题");
            defaultIntent.setChainToCall("chatDirect");
            return defaultIntent;
        }
        // 返回第一个作为默认
        return intents.get(0);
    }
}
