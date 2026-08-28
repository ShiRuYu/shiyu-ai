package com.shiyu.ai.agent.node.intent;
import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
    private static Table<String, String, List<IntentDefinition>> intentTable = HashBasedTable.create();

    static {
        // 意图定义现在由 DB 驱动，启动时由 IntentDefApplicationRunner 从 DB 加载
        // 运行时可通过 reloadFromDb(List<IntentDefBO>) 动态刷新
    }

    /**
     * 从 DB 意图定义数据重新加载 intentTable，清空原有数据后填充。
     *
     * @param boList DB 中查询到的意图定义列表
     */
    public static void reloadFromDb(List<IntentDefBO> boList) {
        intentTable.clear();
        if (boList == null || boList.isEmpty()) {
            return;
        }
        for (IntentDefBO bo : boList) {
            IntentDefinition def = IntentDefinition.builder()
                    .code(bo.getCode())
                    .name(bo.getName())
                    .description(bo.getDescription())
                    .category(bo.getCategory())
                    .priority(bo.getPriority())
                    .confidenceThreshold(bo.getConfidenceThreshold())
                    .examples(bo.getExamples() != null ? bo.getExamples().toArray(new String[0]) : new String[0])
                    .targetNode(bo.getTargetNode())
                    .requireSlotFilling(bo.getRequireSlotFilling() != null && bo.getRequireSlotFilling())
                    .slots(bo.getSlots() != null ? new HashMap<>(bo.getSlots()) : new HashMap<>())
                    .parameterMapping(bo.getParameterMapping() != null ? new HashMap<>(bo.getParameterMapping()) : new HashMap<>())
                    .slotDefaults(bo.getSlotDefaults() != null ? new HashMap<>(bo.getSlotDefaults()) : new HashMap<>())
                    .enabled(bo.getEnabled() != null && bo.getEnabled())
                    .build();
            String row = bo.getAgentId() != null ? bo.getAgentId() : "default";
            String column = bo.getCategory() != null ? bo.getCategory() : "CONVERSATION";
            put(row, column, def);
        }
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
            // A missing custom row may fall back to the default row, but a
            // missing default row must terminate with an empty result rather
            // than recursively asking for the same row forever.
            if (!"default".equals(row)) {
                Map<String, List<IntentDefinition>> defaultRow = intentTable.row("default");
                if (defaultRow != null && !defaultRow.isEmpty()) {
                    rowMap = defaultRow;
                } else {
                    return List.of();
                }
            } else {
                return List.of();
            }
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

    /**
     * 根据指定 agent 和 category 下的所有意图定义，动态构建条件边路由谓词映射。
     * <p>
     * 每个意图定义的 {@link IntentDefinition#getCode()} 作为判断条件，
     * {@link IntentDefinition#getTargetNode()} 作为路由目标。
     * 由此替代硬编码的 {@code addConditionalEdge} 谓词列表，
     * 新增意图时只需 {@link #register(String, String, IntentDefinition)}，路由自动适配。
     *
     * @param agentId  agent ID
     * @param category 意图分类
     * @return 条件边谓词映射（{@link Predicate} → 目标节点 ID）
     */
    public static Map<Predicate<Map<String, Object>>, String> buildRoutingPredicates(
            String agentId, String category) {
        Map<Predicate<Map<String, Object>>, String> routing = new LinkedHashMap<>();
        List<IntentDefinition> defs = getByCategory(agentId, category);
        for (IntentDefinition def : defs) {
            String code = def.getCode();
            String targetNode = def.getTargetNode();
            if (targetNode != null && !targetNode.trim().isEmpty()) {
                routing.put(
                        state -> code.equals(state.get("intentCode")),
                        targetNode
                );
            }
        }
        return routing;
    }
}
