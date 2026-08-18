package com.shiyu.ai.agent.config;

/**
 * 节点编辑器数据源 API 路径常量
 *
 * 前端通过 DataSourceConfig 获取这些路径来调用后端接口，
 * 填充 Agent 编辑器中节点的下拉选项（AI平台、模型、Agent等）。
 */
public final class DataSourceApiConstants {

    private DataSourceApiConstants() {}

    /** AI平台管理（已启用平台列表） */
    public static final String PLATFORM_ENABLED = "/v1/platform/providers/enabled";

    /** 按平台编码获取模型列表 */
    public static final String MODEL_BY_PLATFORM = "/v1/platform/models/platform/by-code";

    /** Agent 列表（全部） */
    public static final String AGENT_LIST_ALL = "/v1/agents/options";

}
