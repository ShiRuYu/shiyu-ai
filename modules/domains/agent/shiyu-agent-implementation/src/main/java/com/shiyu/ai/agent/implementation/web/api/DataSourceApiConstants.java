package com.shiyu.ai.agent.implementation.web.api;

/**
 * 承载 Data Source API Constants 所属 Web 能力的请求适配和边界处理。
 */
public final class DataSourceApiConstants {

    private DataSourceApiConstants() {}

    /** AI平台管理（已启用平台列表） */
    public static final String PLATFORM_ENABLED = "/api/model/platforms/enabled";

    /** 按平台编码获取模型列表 */
    public static final String MODEL_BY_PLATFORM = "/api/model/models/platform/by-code";

    /** Agent 列表（全部） */
    public static final String AGENT_LIST_ALL = "/api/agent/agents/options";
}
