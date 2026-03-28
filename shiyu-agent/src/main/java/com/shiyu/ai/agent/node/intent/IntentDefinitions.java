package com.shiyu.ai.agent.node.intent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 意图定义工厂
 * 用于快速创建和管理常用的意图定义
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
public class IntentDefinitions {
    
    /**
     * 获取所有预定义的意图定义
     *
     * @return 所有意图定义列表
     */
    public static List<IntentDefinition> getAllDefinitions() {
        return Arrays.asList(
            createChitChatDefinition(),
            createQuestionDefinition(),
            createTaskDefinition(),
            createQueryDefinition(),
            createCodeHelpDefinition()
        );
    }
    
    /**
     * 创建闲聊意图定义
     *
     * @return 闲聊意图定义
     */
    public static IntentDefinition createChitChatDefinition() {
        return IntentDefinition.builder()
                .code(IntentType.CHITCHAT.getCode())
                .name(IntentType.CHITCHAT.getName())
                .description(IntentType.CHITCHAT.getDescription())
                .category("CONVERSATION")
                .priority(50)
                .confidenceThreshold(0.75)
                .examples(new String[]{
                        "你好",
                        "最近怎么样",
                        "今天天气不错",
                        "你在干什么",
                        "聊聊天吧"
                })
                .chainToCall("chatDirect")
                .enabled(true)
                .build();
    }
    
    /**
     * 创建问答意图定义
     *
     * @return 问答意图定义
     */
    public static IntentDefinition createQuestionDefinition() {
        return IntentDefinition.builder()
                .code(IntentType.QUESTION.getCode())
                .name(IntentType.QUESTION.getName())
                .description(IntentType.QUESTION.getDescription())
                .category("KNOWLEDGE")
                .priority(60)
                .confidenceThreshold(0.8)
                .examples(new String[]{
                        "什么是人工智能",
                        "为什么天空是蓝色的",
                        "如何学习编程",
                        "地球有多大",
                        "谁发明了电灯"
                })
                .chainToCall("chatWithRag")
                .enabled(true)
                .build();
    }
    
    /**
     * 创建任务型意图定义
     *
     * @return 任务型意图定义
     */
    public static IntentDefinition createTaskDefinition() {
        return IntentDefinition.builder()
                .code(IntentType.TASK.getCode())
                .name(IntentType.TASK.getName())
                .description(IntentType.TASK.getDescription())
                .category("TASK")
                .priority(70)
                .confidenceThreshold(0.85)
                .examples(new String[]{
                        "帮我订一张机票",
                        "设置一个明天早上的闹钟",
                        "发送邮件给张三",
                        "创建一个待办事项",
                        "预约明天的会议"
                })
                .requireSlotFilling(true)
                .chainToCall("chatWithTool")
                .enabled(true)
                .build();
    }
    
    /**
     * 创建查询意图定义
     *
     * @return 查询意图定义
     */
    public static IntentDefinition createQueryDefinition() {
        return IntentDefinition.builder()
                .code(IntentType.QUERY.getCode())
                .name(IntentType.QUERY.getName())
                .description(IntentType.QUERY.getDescription())
                .category("SEARCH")
                .priority(65)
                .confidenceThreshold(0.8)
                .examples(new String[]{
                        "查询我的订单",
                        "看看今天的新闻",
                        "搜索相关的文章",
                        "查找联系人信息",
                        "查看账户余额"
                })
                .chainToCall("chatWithSearch")
                .enabled(true)
                .build();
    }
    
    /**
     * 创建代码帮助意图定义
     *
     * @return 代码帮助意图定义
     */
    public static IntentDefinition createCodeHelpDefinition() {
        IntentDefinition definition = IntentDefinition.builder()
                .code(IntentType.CODE_HELP.getCode())
                .name(IntentType.CODE_HELP.getName())
                .description(IntentType.CODE_HELP.getDescription())
                .category("TECHNICAL")
                .priority(75)
                .confidenceThreshold(0.85)
                .examples(new String[]{
                        "这段代码有什么问题",
                        "如何优化这个算法",
                        "解释一下这个函数",
                        "帮我写一个排序方法",
                        "这个错误怎么解决"
                })
                .slots(Map.of(
                                "language", "编程语言",
                                "codeSnippet", "代码片段"
                        ))
                .requireSlotFilling(false)
                .chainToCall("chatWithCode")
                .enabled(true)
                .build();
        
        // 添加槽位定义
        definition.addSlot("language", "编程语言");
        definition.addSlot("codeSnippet", "代码片段");
        
        return definition;
    }
    
    /**
     * 根据分类获取意图定义
     *
     * @param category 分类名称
     * @return 该分类下的所有意图定义
     */
    public static List<IntentDefinition> getByCategory(String category) {
        return getAllDefinitions().stream()
                .filter(def -> def.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据代码获取意图定义
     *
     * @param code 意图代码
     * @return 对应的意图定义，如果未找到则返回 null
     */
    public static IntentDefinition getByCode(String code) {
        return getAllDefinitions().stream()
                .filter(def -> def.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
