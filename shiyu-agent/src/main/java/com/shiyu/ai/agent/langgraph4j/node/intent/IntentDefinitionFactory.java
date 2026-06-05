package com.shiyu.ai.agent.langgraph4j.node.intent;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 意图定义工厂
 * <p>
 * 使用 Guava {@link HashBasedTable} 管理意图定义，rowKey = agentId，columnKey = category。
 * 支持按 agentId 和 category 查询对应的意图定义列表。
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
public class IntentDefinitionFactory {

    /**
     * intentTable: row = agentId, column = category, value = IntentDefinition
     */
    private static final Table<String, String, IntentDefinition> intentTable = HashBasedTable.create();

    static {
        // ========== 默认 agent ("default") ==========

        // CONVERSATION 分类
        intentTable.put("default", "CONVERSATION", IntentDefinition.builder()
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
                .targetNode("chatDirect")
                .enabled(true)
                .build());

        // KNOWLEDGE 分类
        intentTable.put("default", "KNOWLEDGE", IntentDefinition.builder()
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
                .targetNode("chatWithRag")
                .enabled(true)
                .build());

        // TASK 分类
        intentTable.put("default", "TASK", IntentDefinition.builder()
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
                .targetNode("chatWithTool")
                .enabled(true)
                .build());

        // SEARCH 分类
        intentTable.put("default", "SEARCH", IntentDefinition.builder()
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
                .targetNode("chatWithSearch")
                .enabled(true)
                .build());

        // TECHNICAL 分类
        IntentDefinition codeHelp = IntentDefinition.builder()
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
                .targetNode("chatWithCode")
                .enabled(true)
                .build();
        codeHelp.addSlot("language", "编程语言");
        codeHelp.addSlot("codeSnippet", "代码片段");
        intentTable.put("default", "TECHNICAL", codeHelp);
    }

    // ==================== 查询方法 ====================

    /**
     * 根据 agentId 和 category 获取对应的意图定义
     *
     * @param agentId  代理 ID
     * @param category 意图分类
     * @return 对应的意图定义，不存在时返回 {@code null}
     */
    public static IntentDefinition get(String agentId, String category) {
        IntentDefinition def = intentTable.get(agentId, category);
        if (def == null) {
            // fallback 到 default agent
            return intentTable.get("default", category);
        }
        return def;
    }

    /**
     * 根据 agentId 获取该代理所有分类下的意图定义
     *
     * @param agentId 代理 ID
     * @return 所有意图定义（不可变列表）
     */
    public static List<IntentDefinition> getAll(String agentId) {
        Map<String, IntentDefinition> row = intentTable.row(agentId);
        if (row == null || row.isEmpty()) {
            return getAll("default");
        }
        return List.copyOf(row.values());
    }

    /**
     * 根据 category 获取所有代理的意图定义（跨 agent）
     *
     * @param category 意图分类
     * @return 该分类下的所有意图定义（不可变列表）
     */
    public static List<IntentDefinition> getByCategory(String category) {
        Map<String, IntentDefinition> column = intentTable.column(category);
        return List.copyOf(column.values());
    }

    /**
     * 根据 agentId + category 获取该分类下的所有意图定义
     *
     * @param agentId  代理 ID
     * @param category 意图分类
     * @return 该 agent 下该分类的意图定义列表（只含一条或为空）
     */
    public static List<IntentDefinition> getByCategory(String agentId, String category) {
        IntentDefinition def = get(agentId, category);
        if (def != null) {
            return List.of(def);
        }
        return List.of();
    }

    /**
     * 注册自定义意图定义
     *
     * @param agentId  代理 ID
     * @param category 意图分类
     * @param def      意图定义
     */
    public static void register(String agentId, String category, IntentDefinition def) {
        intentTable.put(agentId, category, def);
    }

    /**
     * 获取所有已知的 agentId
     *
     * @return agentId 集合
     */
    public static Set<String> getAgentIds() {
        return Collections.unmodifiableSet(intentTable.rowKeySet());
    }

    /**
     * 获取所有已知的 category
     *
     * @return category 集合
     */
    public static Set<String> getCategories() {
        return Collections.unmodifiableSet(intentTable.columnKeySet());
    }
}
