package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.repository.AgentAdminRepository;
import com.shiyu.ai.agent.biz.agent.service.AgentAdminService;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
import com.shiyu.ai.agent.domain.bo.AgentDefBO;
import com.shiyu.ai.agent.domain.bo.AgentVersionBO;
import com.shiyu.ai.agent.domain.request.AgentRequest;
import com.shiyu.ai.agent.domain.vo.*;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentAdminServiceImpl implements AgentAdminService {

    @Resource
    private AgentAdminRepository agentAdminRepository;

    @Resource
    private AgentService agentService;

    @Override
    public Pair<Long, List<AgentVO>> getPage(Number pageNo, Number pageSize, String name, String status) {
        Pair<Long, List<AgentDefBO>> result = agentAdminRepository.selectPage(pageNo, pageSize, name, status);
        List<AgentVO> vos = result.getRight().stream().map(this::toVO).collect(Collectors.toList());
        return Pair.of(result.getLeft(), vos);
    }

    @Override
    public AgentDetailVO getById(Long id) {
        AgentDefBO def = agentAdminRepository.selectById(id);
        if (def == null) return null;
        List<AgentVersionBO> versions = agentAdminRepository.selectVersionsByAgentId(def.getAgentId());
        List<AgentVersionVO> versionVOs = versions.stream().map(this::toVersionVO).collect(Collectors.toList());
        return AgentDetailVO.builder()
                .id(def.getId()).agentId(def.getAgentId()).name(def.getName())
                .description(def.getDescription()).currentVersion(def.getCurrentVersion())
                .status(def.getStatus()).versions(versionVOs)
                .createTime(def.getCreateTime()).updateTime(def.getUpdateTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVO create(AgentRequest request) {
        AgentDefBO existing = agentAdminRepository.selectByAgentId(request.getAgentId());
        if (existing != null) {
            throw new IllegalArgumentException("Agent标识已存在: " + request.getAgentId());
        }
        AgentDefBO def = new AgentDefBO();
        def.setAgentId(request.getAgentId());
        def.setName(request.getName());
        def.setDescription(request.getDescription());
        def.setStatus(request.getStatus() != null ? request.getStatus() : "1");
        agentAdminRepository.create(def);
        return toVO(def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVO update(Long id, AgentRequest request) {
        AgentDefBO def = agentAdminRepository.selectById(id);
        if (def == null) throw new IllegalArgumentException("Agent不存在: " + id);
        if (request.getName() != null) def.setName(request.getName());
        if (request.getDescription() != null) def.setDescription(request.getDescription());
        if (request.getStatus() != null) def.setStatus(request.getStatus());
        def.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.update(def);
        evictAgentCache(def.getAgentId());
        return toVO(def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        AgentDefBO def = agentAdminRepository.selectById(id);
        if (def == null) return;
        agentAdminRepository.deleteById(id);
        evictAgentCache(def.getAgentId());
    }

    @Override
    public List<NodeTypeMetaVO> getNodeTypes() {
        List<NodeTypeMetaVO> metas = new ArrayList<>();
        for (NodeType type : NodeType.values()) {
            metas.add(NodeTypeMetaVO.builder()
                    .code(type.getCode())
                    .name(type.getName())
                    .description(type.getDescription())
                    .icon("icon-" + type.getCode().toLowerCase().replace("_", "-"))
                    .color(getNodeColor(type))
                    .fields(buildFieldMetas(type))
                    .build());
        }
        return metas;
    }

    @Override
    public List<IdNameOptionVO> listAllOptions() {
        List<AgentDefBO> list = agentAdminRepository.selectAllActive();
        return list.stream().map(d -> IdNameOptionVO.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getAgentId())
                .build()).collect(Collectors.toList());
    }

    // ========== Private helpers ==========

    private void evictAgentCache(String agentId) {
        agentService.evictRuntimeCache(agentId);
    }

    private AgentVO toVO(AgentDefBO def) {
        return AgentVO.builder()
                .id(def.getId()).agentId(def.getAgentId()).name(def.getName())
                .description(def.getDescription()).currentVersion(def.getCurrentVersion())
                .status(def.getStatus())
                .createTime(def.getCreateTime()).updateTime(def.getUpdateTime())
                .build();
    }

    private AgentVersionVO toVersionVO(AgentVersionBO v) {
        return AgentVersionVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    private String getNodeColor(NodeType type) {
        return switch (type) {
            case INTENT -> "#FF9800";
            case LLM_CALL -> "#4CAF50";
            case RAG_RETRIEVAL, RAG_ENHANCEMENT -> "#2196F3";
            case TOOL_CALL -> "#9C27B0";
            case CONDITION -> "#FF5722";
            case TRANSFORM -> "#607D8B";
            case OUTPUT_FORMAT -> "#00BCD4";
            case MEMORY_SHORT_TERM, MEMORY_LONG_TERM, MEMORY_RETRIEVAL -> "#795548";
            case AGENT_CALL -> "#E91E63";
            default -> "#757575";
        };
    }

    private List<NodeTypeMetaVO.FieldMeta> buildFieldMetas(NodeType type) {
        List<NodeTypeMetaVO.FieldMeta> fields = new ArrayList<>();
        switch (type) {
            case INTENT:
                fields.add(fieldWithSource(field("category", "意图分类", "select", "", false, "意图分类"),
                    new NodeTypeMetaVO.DataSourceConfig("dict", null, "INTENT_CATEGORY", "dictLabel", "dictValue", null)));
                fields.add(field("confidenceThreshold", "置信度阈值", "number", 0.75, false, "意图识别的最低置信度"));
                fields.add(fieldWithSource(field("platform", "AI平台", "select", "", false, "选择AI平台"),
                    new NodeTypeMetaVO.DataSourceConfig("api", "/ai/platform/enabled", null, "name", "code", null)));
                fields.add(fieldWithSource(field("modelName", "模型名称", "select", "", false, "选择模型"),
                    new NodeTypeMetaVO.DataSourceConfig("api", "/ai/model/platform/by-code/{platform}", null, "displayName", "modelName", "platform")));
                break;
            case LLM_CALL:
                fields.add(fieldWithSource(field("platform", "AI平台", "select", "", false, "选择AI平台"),
                    new NodeTypeMetaVO.DataSourceConfig("api", "/ai/platform/enabled", null, "name", "code", null)));
                fields.add(fieldWithSource(field("modelName", "模型名称", "select", "", false, "选择模型"),
                    new NodeTypeMetaVO.DataSourceConfig("api", "/ai/model/platform/by-code/{platform}", null, "displayName", "modelName", "platform")));
                fields.add(field("temperature", "温度参数", "number", 0.7, false, "控制输出随机性(0-2)"));
                fields.add(field("maxTokens", "最大Token数", "number", 4096, false, "输出最大长度"));
                fields.add(field("topP", "Top-P", "number", 0.9, false, "核采样参数"));
                fields.add(field("systemPrompt", "系统提示词", "textarea", "", false, "系统级别的指令"));
                fields.add(field("defaultPrompt", "默认提示词", "textarea", "", false, "默认的用户提示词"));
                fields.add(field("promptTemplate", "提示词模板", "textarea", "", false, "支持{context}{query}占位符"));
                fields.add(field("stream", "流式输出", "boolean", false, false, "是否使用流式输出"));
                break;
            case RAG_RETRIEVAL:
                fields.add(field("knowledgeBaseId", "知识库ID", "text", "", false, "目标知识库ID"));
                fields.add(field("topK", "检索数量", "number", 5, false, "返回的相关文档数量"));
                fields.add(field("similarityThreshold", "相似度阈值", "number", 0.7, false, "最低相似度"));
                fields.add(field("enableRerank", "重排序", "boolean", false, false, "是否启用重排序"));
                break;
            case RAG_ENHANCEMENT:
                fields.add(field("enhancementStrategy", "增强策略", "select", "SUMMARIZATION", false, Map.of("options", List.of("SUMMARIZATION", "CHUNKING", "HYBRID"))));
                fields.add(field("contextWindowSize", "上下文窗口", "number", 3, false, "上下文窗口大小"));
                fields.add(field("maxLength", "最大长度", "number", 2000, false, "输出最大长度"));
                fields.add(field("addContext", "添加上下文", "boolean", true, false, "是否添加上下文"));
                break;
            case TOOL_CALL:
                fields.add(field("toolName", "工具名称", "text", "", true, "调用的工具标识"));
                fields.add(field("toolType", "工具类型", "text", "", false, "工具类型分类"));
                fields.add(field("toolTimeout", "超时时间(ms)", "number", 10000L, false, "工具调用超时"));
                fields.add(field("enableCache", "启用缓存", "boolean", false, false, "是否缓存工具结果"));
                break;
            case CONDITION:
                fields.add(field("conditionExpression", "条件表达式", "textarea", "", false, "条件判断表达式"));
                fields.add(field("conditionType", "条件类型", "select", "EXPRESSION", false, Map.of("options", List.of("EXPRESSION", "INTENT_ROUTING"))));
                break;
            case TRANSFORM:
                fields.add(field("transformType", "转换类型", "select", "JSON_TO_XML", false, Map.of("options", List.of("JSON_TO_XML", "XML_TO_JSON", "TEMPLATE"))));
                fields.add(field("template", "转换模板", "textarea", "", false, "转换规则模板"));
                break;
            case OUTPUT_FORMAT:
                fields.add(field("outputFormat", "输出格式", "select", "TEXT", false, Map.of("options", List.of("TEXT", "JSON", "MARKDOWN", "HTML"))));
                fields.add(field("template", "格式化模板", "textarea", "", false, "输出格式化模板"));
                fields.add(field("prettyPrint", "美化输出", "boolean", true, false, "是否美化格式"));
                break;
            case MEMORY_SHORT_TERM:
                fields.add(field("maxMessages", "最大消息数", "number", 10, false, "短时记忆窗口大小"));
                fields.add(field("enableSlidingWindow", "滑动窗口", "boolean", true, false, "是否启用滑动窗口"));
                fields.add(field("messageExpiryTime", "消息过期时间(ms)", "number", 3600000L, false, "消息过期时间"));
                break;
            case AGENT_CALL:
                fields.add(fieldWithSource(field("targetAgentId", "目标Agent", "select", "", true, "选择已存在的Agent"),
                    new NodeTypeMetaVO.DataSourceConfig("api", "/admin/agent/list/all", null, "name", "code", null)));
                fields.add(field("agentTimeout", "超时时间(ms)", "number", 30000L, false, "Agent调用超时"));
                fields.add(field("async", "异步调用", "boolean", false, false, "是否异步调用"));
                break;
            default:
                break;
        }
        return fields;
    }

    private NodeTypeMetaVO.FieldMeta field(String key, String label, String type, Object defaultValue, boolean required, Object extra) {
        return NodeTypeMetaVO.FieldMeta.builder()
                .key(key).label(label).type(type).defaultValue(defaultValue).required(required)
                .description("")
                .options(extra instanceof Map ? (Map<String, Object>) extra : null)
                .build();
    }

    private NodeTypeMetaVO.FieldMeta fieldWithSource(NodeTypeMetaVO.FieldMeta meta, NodeTypeMetaVO.DataSourceConfig source) {
        meta.setSource(source);
        return meta;
    }
}
