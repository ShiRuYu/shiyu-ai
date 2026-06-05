package com.shiyu.ai.agent.langgraph4j.node.intent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 意图定义工厂
 * <p>
 * 使用 Guava {@link HashBasedTable} 管理意图定义，rowKey = row，columnKey = column。
 * 每个 (row, column) 单元格持有 {@link List}&lt;{@link IntentDefinition}&gt;，
 * 支持同一分类下注册多个意图定义。
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
public class IntentDefinitionFactory {

    /**
     * intentTable: row = agentId, column = category, value = List&lt;IntentDefinition&gt;
     */
    private static final Table<String, String, List<IntentDefinition>> intentTable = HashBasedTable.create();

    static {
        // ========== 默认 agent ("default") ==========

        // CONVERSATION 分类
        put("default", "CONVERSATION", IntentDefinition.builder()
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
        put("default", "KNOWLEDGE", IntentDefinition.builder()
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
        put("default", "TASK", IntentDefinition.builder()
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
        put("default", "SEARCH", IntentDefinition.builder()
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
        put("default", "TECHNICAL", codeHelp);
    }

    // ==================== 内部辅助 ====================

    /**
     * 向表中追加一条意图定义，如果该 (row, column) 已存在列表则追加，否则新建列表。
     */
    private static void put(String row, String column, IntentDefinition def) {
        List<IntentDefinition> list = intentTable.get(row, column);
        if (list == null) {
            list = new ArrayList<>();
            intentTable.put(row, column, list);
        }
        list.add(def);
    }

    // ==================== 查询方法 ====================

    /**
     * 根据 row 和 column 获取该分类下的所有意图定义
     *
     * @param row    row key（agentId）
     * @param column column key（意图分类）
     * @return 意图定义列表（不可变），不存在时返回空列表
     */
    public static List<IntentDefinition> getByCategory(String row, String column) {
        // 先查指定 row
        List<IntentDefinition> list = intentTable.get(row, column);
        if (list != null && !list.isEmpty()) {
            return Collections.unmodifiableList(list);
        }
        // fallback 到 default row
        list = intentTable.get("default", column);
        if (list != null && !list.isEmpty()) {
            return Collections.unmodifiableList(list);
        }
        return List.of();
    }

    /**
     * 根据 row 获取该 row 下所有 column 的意图定义
     *
     * @param row row key（agentId）
     * @return 所有意图定义（合并列表，不可变）
     */
    public static List<IntentDefinition> getAll(String row) {
        Map<String, List<IntentDefinition>> rowMap = intentTable.row(row);
        if (rowMap == null || rowMap.isEmpty()) {
            return getAll("default");
        }
        List<IntentDefinition> result = new ArrayList<>();
        for (List<IntentDefinition> list : rowMap.values()) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 根据 column 获取所有 row 的意图定义（跨 agent）
     *
     * @param column 意图分类
     * @return 该分类下的所有意图定义（合并列表，不可变）
     */
    public static List<IntentDefinition> getByCategory(String column) {
        Map<String, List<IntentDefinition>> columnMap = intentTable.column(column);
        List<IntentDefinition> result = new ArrayList<>();
        for (List<IntentDefinition> list : columnMap.values()) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 根据 row + column 获取第一个匹配的意图定义
     *
     * @param row    row key（agentId）
     * @param column column key（意图分类）
     * @return 第一个意图定义，不存在时返回 {@code null}
     */
    public static IntentDefinition getFirst(String row, String column) {
        List<IntentDefinition> list = getByCategory(row, column);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 注册自定义意图定义（添加到已有列表或新建列表）
     *
     * @param row    row key（agentId）
     * @param column column key（意图分类）
     * @param def    意图定义
     */
    public static void register(String row, String column, IntentDefinition def) {
        put(row, column, def);
    }

    /**
     * 获取所有已知的 row key（agentId）
     *
     * @return row key 集合
     */
    public static Set<String> getAgentIds() {
        return Collections.unmodifiableSet(intentTable.rowKeySet());
    }

    /**
     * 获取所有已知的 column key（category）
     *
     * @return column key 集合
     */
    public static Set<String> getCategories() {
        return Collections.unmodifiableSet(intentTable.columnKeySet());
    }
}
