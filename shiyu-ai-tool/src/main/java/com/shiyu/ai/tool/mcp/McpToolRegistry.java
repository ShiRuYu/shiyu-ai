package com.shiyu.ai.tool.mcp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MCP 工具注册表
 * 支持工具的注册、注销、发现、缓存
 */
@Slf4j
public class McpToolRegistry {

    /** 工具名 → 描述符 */
    private final Map<String, McpToolDescriptor> toolMap = new ConcurrentHashMap<>();

    /** 分类 → 工具名列表 */
    private final Map<String, Set<String>> categoryIndex = new ConcurrentHashMap<>();

    /** 标签 → 工具名列表 */
    private final Map<String, Set<String>> tagIndex = new ConcurrentHashMap<>();

    /** 已发现工具缓存（短时缓存避免重复发现） */
    private final Cache<String, List<McpToolDescriptor>> discoveryCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    // ======================== 注册管理 ========================

    /**
     * 注册一个工具
     */
    public void register(McpToolDescriptor tool) {
        toolMap.put(tool.getName(), tool);

        // 维护分类索引
        categoryIndex.computeIfAbsent(tool.getCategory(), k -> ConcurrentHashMap.newKeySet())
                .add(tool.getName());

        // 维护标签索引
        if (tool.getTags() != null) {
            for (String tag : tool.getTags()) {
                tagIndex.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet())
                        .add(tool.getName());
            }
        }

        discoveryCache.invalidateAll();
        log.debug("工具已注册: {} ({})", tool.getName(), tool.getCategory());
    }

    /**
     * 批量注册工具
     */
    public void registerAll(List<McpToolDescriptor> tools) {
        tools.forEach(this::register);
    }

    /**
     * 注销工具
     */
    public void unregister(String name) {
        McpToolDescriptor removed = toolMap.remove(name);
        if (removed != null) {
            categoryIndex.getOrDefault(removed.getCategory(), Collections.emptySet()).remove(name);
            if (removed.getTags() != null) {
                for (String tag : removed.getTags()) {
                    tagIndex.getOrDefault(tag, Collections.emptySet()).remove(name);
                }
            }
            discoveryCache.invalidateAll();
            log.info("工具已注销: {}", name);
        }
    }

    /**
     * 获取工具描述
     */
    public McpToolDescriptor getTool(String name) {
        return toolMap.get(name);
    }

    /**
     * 获取所有工具
     */
    public List<McpToolDescriptor> listTools() {
        return new ArrayList<>(toolMap.values());
    }

    /**
     * 按分类查询工具
     */
    public List<McpToolDescriptor> getToolsByCategory(String category) {
        Set<String> names = categoryIndex.getOrDefault(category, Collections.emptySet());
        return names.stream()
                .map(toolMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 按标签查询工具
     */
    public List<McpToolDescriptor> getToolsByTag(String tag) {
        Set<String> names = tagIndex.getOrDefault(tag, Collections.emptySet());
        return names.stream()
                .map(toolMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 搜索工具（按名称/描述模糊匹配）
     */
    public List<McpToolDescriptor> searchTools(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listTools();
        }
        String lower = keyword.toLowerCase();
        return toolMap.values().stream()
                .filter(t -> t.getName().toLowerCase().contains(lower)
                        || t.getDescription().toLowerCase().contains(lower)
                        || t.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分类
     */
    public Set<String> getCategories() {
        return categoryIndex.keySet();
    }

    /**
     * 获取工具数量
     */
    public int size() {
        return toolMap.size();
    }
}
