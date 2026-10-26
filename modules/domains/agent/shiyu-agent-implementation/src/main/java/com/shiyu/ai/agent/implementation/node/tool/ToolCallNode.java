package com.shiyu.ai.agent.implementation.node.tool;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.kernel.context.ActorContext;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;
import com.shiyu.ai.agent.implementation.checkpoint.AgentGraphCheckpointSaver;
import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.tooling.contract.api.ToolService.ToolExecutionContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行 工具 Call 相关流程节点的输入处理和状态转移。
 *
 * @author shiyu-ai
 */
@Setter
@Getter
@Slf4j
public class ToolCallNode extends BaseNode {

    private static final java.util.regex.Pattern URL_USER_INFO =
            java.util.regex.Pattern.compile("(?i)(https?://)[^/@\\s]+@");
    private static final java.util.regex.Pattern SENSITIVE_QUERY_PARAMETER =
            java.util.regex.Pattern.compile(
                    "(?i)([?&][^=&#]*(?:password|passwd|passphrase|secret|token|api[_-]?key|access[_-]?key|authorization|credential|cookie|session|jwt|signature)[^=&#]*=)[^&#]*");
    private static final java.util.regex.Pattern SHORT_SIGNATURE_QUERY_PARAMETER =
            java.util.regex.Pattern.compile("(?i)([?&](?:sig|[a-z0-9]+[_-]sig)=)[^&#]*");
    private static final java.util.regex.Pattern COOKIE_HEADER =
            java.util.regex.Pattern.compile("(?i)(\\bcookie\\s*[:=]\\s*)[^\\r\\n]+");
    private static final java.util.regex.Pattern AUTHORIZATION_VALUE =
            java.util.regex.Pattern.compile("(?i)(\\b(?:authorization|auth)[\"']?\\s*[:=]\\s*)[^\\r\\n]+");
    private static final java.util.regex.Pattern BEARER_TOKEN =
            java.util.regex.Pattern.compile("(?i)\\bbearer\\s+[a-z0-9._~+/-]{8,}={0,2}");
    private static final java.util.regex.Pattern SENSITIVE_ASSIGNMENT =
            java.util.regex.Pattern.compile(
                    "(?i)(\\b(?:auth|password|passwd|passphrase|secret|token|api[_-]?key|access[_-]?key|authorization|credential|cookie|session(?:[_-]?id)?|jwt|private[_-]?key|signature|sig)[\"']?\\s*[:=]\\s*)(?:\"[^\"]*\"|'[^']*'|[^\\s,;&}]+)");
    private static final java.util.regex.Pattern JWT_VALUE =
            java.util.regex.Pattern.compile("\\beyJ[a-zA-Z0-9_-]{5,}\\.[a-zA-Z0-9_-]{5,}\\.[a-zA-Z0-9_-]{5,}\\b");
    private static final java.util.regex.Pattern API_KEY_VALUE =
            java.util.regex.Pattern.compile("(?i)\\bsk-[a-z0-9_-]{16,}\\b");

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private ToolCallConfig config;

    /** 工具调用服务（必须依赖） */
    private final ToolService toolService;

    /**
     * executionPipeline 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolExecutionPipeline executionPipeline;

    /**
     * 私有构造函数，强制使用 Builder 模式
     *
     * @param config 节点配置
     * @param toolService 工具调用服务
     */
    private ToolCallNode(
            ToolCallConfig config,
            ToolService toolService,
            ToolExecutionPipeline executionPipeline) {
        super(config != null ? config : new ToolCallConfig());
        this.config = config != null ? config : new ToolCallConfig();
        // Tool calls can have external side effects: fail the graph and never replay the node.
        this.config.setNodeType(NodeType.TOOL_CALL);
        this.config.setRetryCount(0);
        this.config.setErrorStrategy("THROW");
        this.toolService = toolService;
        this.executionPipeline = executionPipeline;
    }

    /**
     * 获取 Builder 实例
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建 Builder 相关的对象、流程或运行时配置。
     */
    public static class Builder {
        private ToolCallConfig config;
        /**
         * toolService 属性，保存当前对象中的业务数据或协作依赖。
         */
        private ToolService toolService;
        /**
         * executionPipeline 属性，保存当前对象中的业务数据或协作依赖。
         */
        private ToolExecutionPipeline executionPipeline;

        /**
         * 设置节点配置
         *
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(ToolCallConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置工具调用服务
         *
         * @param toolService 工具调用服务
         * @return Builder 实例
         */
        public Builder toolService(ToolService toolService) {
            this.toolService = toolService;
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param executionPipeline 用于完成本次业务处理的 executionPipeline 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder executionPipeline(ToolExecutionPipeline executionPipeline) {
            this.executionPipeline = executionPipeline;
            return this;
        }

        /**
         * 构建并返回 ToolCallNode 实例 在构建前会进行必要的校验
         *
         * @return ToolCallNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public ToolCallNode build() {
            // 校验：toolService 不能为空
            if (toolService == null) {
                throw new IllegalStateException("创建 ToolCallNode 失败：toolService 不能为空");
            }

            // 所有校验通过，创建并返回实例
            return new ToolCallNode(config, toolService, executionPipeline);
        }
    }

    /**
     * 执行 工具 Call 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 工具 Call 相关操作生成的结果数据。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行工具调用节点：{}", config.getNodeName());
        log.debug(
                "工具配置：toolName={}, toolType={}, timeout={}",
                config.getToolName(),
                config.getToolType(),
                config.getToolTimeout());

        try {
            // 1. 获取工具名称和参数
            String toolName = getToolName(input);

            if (toolName == null || toolName.trim().isEmpty()) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("工具名称不能为空");
                return output;
            }

            // 2. 准备参数
            java.util.Map<String, Object> parameters = prepareToolParameters(input);

            // 3. 调用工具服务
            ToolService.ToolExecutionResult result;
            Object runtimeRun = input.getParameter("_aiRun", null);
            Object runtimeRunId = input.getParameter("__aiRunId", null);
            boolean highRisk =
                    "HIGH".equalsIgnoreCase(config.getToolType())
                            || "HIGH_RISK".equalsIgnoreCase(config.getToolType())
                            || toolService.requiresApproval(toolName);
            if (highRisk
                    && (executionPipeline == null
                            || (!(runtimeRun instanceof AiRun) && runtimeRunId == null))) {
                NodeOutput unavailable = new NodeOutput();
                unavailable.setSuccess(false);
                unavailable.setMsg("高风险工具审批不可用");
                return unavailable;
            }
            if (executionPipeline != null
                    && (runtimeRun instanceof AiRun || runtimeRunId != null)) {
                ActorContext actor = NodeActorContext.require(input.toMap());
                ToolExecutionPipeline.Request request =
                        new ToolExecutionPipeline.Request(
                                toolName,
                                parameters,
                                JSONUtils.toJsonString(redactArguments(parameters)),
                                highRisk,
                                input.getParameter("_toolApprovalId",
                                        input.getParameter(AgentGraphCheckpointSaver.APPROVAL_ID_KEY,
                                                null)));
                ToolExecutionPipeline.Result pipelineResult;
                if (runtimeRun instanceof AiRun run) {
                    if (!actor.tenantId().equals(run.tenantId())
                            || actor.userId().value() != run.ownerUserId().value()) {
                        throw new IllegalStateException("runtime run owner does not match authenticated actor");
                    }
                    pipelineResult =
                            executionPipeline.executeWithContext(
                                    run, request, (args, context) ->
                                            executeTool(toolName, args, context));
                } else {
                    pipelineResult =
                            executionPipeline.executeByIdWithContext(
                                    String.valueOf(runtimeRunId),
                                    actor.tenantId(),
                                    actor.userId().value(),
                                    request,
                                    (args, context) -> executeTool(toolName, args, context));
                }
                if (pipelineResult.status()
                        == ToolExecutionPipeline.Result.Status.APPROVAL_REQUIRED) {
                    throw new NodeApprovalRequiredException(pipelineResult.approval().id());
                }
                result = new ToolService.ToolExecutionResult(true, pipelineResult.value(), null);
            } else {
                result = toolService.execute(toolName, parameters);
            }

            if (!result.success()) {
                log.error(
                        "工具调用失败：errorMessageLength={}",
                        result.errorMessage() == null ? 0 : result.errorMessage().length());
                throw new IllegalStateException(
                        "tool execution failed; side effect outcome=UNCERTAIN; automatic replay disabled");
            }

            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("工具调用成功");
            output.addData(FieldKey.TOOL_NAME, toolName);
            output.addData(FieldKey.TOOL_RESULT, normalizeToolResult(result.result()));
            log.info("工具调用成功：{}", toolName);
            log.info("工具调用节点执行完成");
            return output;

        } catch (NodeApprovalRequiredException pending) {
            throw pending;
        } catch (Exception e) {
            log.error("工具调用节点执行失败", e);
            throw new IllegalStateException(
                    "tool execution failed; side effect outcome=UNCERTAIN; automatic replay disabled",
                    e);
        }
    }

    private Object executeTool(String toolName, java.util.Map<String, Object> args) {
        ToolService.ToolExecutionResult value = toolService.execute(toolName, args);
        if (!value.success()) throw new IllegalStateException(value.errorMessage());
        return value.result();
    }

    private Object executeTool(
            String toolName,
            java.util.Map<String, Object> args,
            ToolExecutionContext context) {
        ToolService.ToolExecutionResult value = toolService.execute(toolName, args, context);
        if (!value.success()) throw new IllegalStateException(value.errorMessage());
        return value.result();
    }

    /**
     * 将工具返回值转换为图状态可克隆的 JSON 值，避免任意对象使图检查点失败。
     *
     * @param value 工具服务返回的数据
     * @return 字符串、数值、布尔值、列表或映射组成的图状态值
     */
    private Object normalizeToolResult(Object value) {
        return value == null ? null
                : JSONUtils.parseObject(JSONUtils.toJsonString(value), Object.class);
    }

    /**
     * 获取工具名称
     *
     * <p>从配置中读取 toolName，每个 ToolCallNode 实例只对应一个工具。
     */
    private String getToolName(NodeInput input) {
        return config.getToolName();
    }

    /**
     * 准备工具参数
     *
     * <p>从 input 中提取参数并处理：
     *
     * <ol>
     *   <li>提取原始参数，排除元数据字段
     *   <li>将 {@link FieldKey#SLOTS} 展平为独立字段
     *   <li>通过 {@link FieldKey#PARAMETER_MAPPING} 重命名 slot key
     *   <li>通过 {@link FieldKey#SLOT_DEFAULTS} 补充缺失的默认值
     *   <li>通过 {@link FieldKey#SLOT_DEFINITIONS} 校验部分 slot 缺失
     * </ol>
     *
     * <p>参数映射/默认值/schema 均由上游 {@code IntentNode} 从 {@code IntentDefinition} 解析后 写入 state
     * 传递至此，不再直接查询工厂。
     */
    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> prepareToolParameters(NodeInput input) {
        // 1. 从 state 读取上游 IntentNode 传递的配置
        java.util.Map<String, String> parameterMapping =
                input.getParameter(FieldKey.PARAMETER_MAPPING, null);
        java.util.Map<String, String> slotDefaults =
                input.getParameter(FieldKey.SLOT_DEFAULTS, null);
        java.util.Map<String, String> slotDefinitions =
                input.getParameter(FieldKey.SLOT_DEFINITIONS, null);

        // 2. 收集原始参数，排除元数据
        java.util.Map<String, Object> raw = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Object> entry : input.toMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 排除元数据字段
            if (FieldKey.TOOL_NAME.key().equals(key)
                    || FieldKey.TOOL_TYPE.key().equals(key)
                    || FieldKey.INTENT_CODE.key().equals(key)
                    || FieldKey.INTENT_NAME.key().equals(key)
                    || FieldKey.CONFIDENCE.key().equals(key)
                    || FieldKey.PARAMETER_MAPPING.key().equals(key)
                    || FieldKey.SLOT_DEFAULTS.key().equals(key)
                    || FieldKey.SLOT_DEFINITIONS.key().equals(key)
                    || AgentGraphCheckpointSaver.APPROVAL_ID_KEY.equals(key)
                    || key.startsWith("_")) {
                continue;
            }

            // 展平 slots 为独立字段
            if (FieldKey.SLOTS.key().equals(key) && value instanceof java.util.Map) {
                java.util.Map<String, Object> slots = (java.util.Map<String, Object>) value;
                raw.putAll(slots);
                continue;
            }

            raw.put(key, value);
        }

        // 3. 参数重命名（slot名 → 工具参数名）
        if (parameterMapping != null && !parameterMapping.isEmpty()) {
            for (java.util.Map.Entry<String, String> me : parameterMapping.entrySet()) {
                String slotName = me.getKey();
                String paramName = me.getValue();
                if (raw.containsKey(slotName) && !slotName.equals(paramName)) {
                    raw.put(paramName, raw.remove(slotName));
                }
            }
        }

        // 4. 补充 slot 默认值（仅当缺失时）
        if (slotDefaults != null && !slotDefaults.isEmpty()) {
            for (java.util.Map.Entry<String, String> de : slotDefaults.entrySet()) {
                String slotName = de.getKey();
                String effectiveKey =
                        parameterMapping != null
                                ? parameterMapping.getOrDefault(slotName, slotName)
                                : slotName;
                if (!raw.containsKey(effectiveKey)) {
                    raw.put(effectiveKey, de.getValue());
                    log.debug("补充 slot 默认值: {}={}", effectiveKey, de.getValue());
                }
            }
        }

        // 5. 校验部分 slot 缺失（仅 warn，不阻断）
        if (slotDefinitions != null && !slotDefinitions.isEmpty()) {
            for (String slotName : slotDefinitions.keySet()) {
                String effectiveKey =
                        parameterMapping != null
                                ? parameterMapping.getOrDefault(slotName, slotName)
                                : slotName;
                if (!raw.containsKey(effectiveKey)
                        || raw.get(effectiveKey) == null
                        || "".equals(raw.get(effectiveKey).toString().trim())) {
                    log.warn("部分 slot 缺失: {} (原始slot名={})", effectiveKey, slotName);
                }
            }
        }

        return raw;
    }

    private static java.util.Map<String, Object> redactArguments(
            java.util.Map<String, Object> arguments) {
        java.util.Map<String, Object> redacted = new java.util.TreeMap<>();
        arguments.forEach((key, value) -> redacted.put(key, redactArgument(key, value)));
        return redacted;
    }

    private static Object redactArgument(String key, Object value) {
        String normalizedKey = key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(java.util.Locale.ROOT);
        if (normalizedKey.contains("password")
                || normalizedKey.equals("auth")
                || normalizedKey.contains("passwd")
                || normalizedKey.contains("passphrase")
                || normalizedKey.contains("secret")
                || normalizedKey.contains("token")
                || normalizedKey.contains("apikey")
                || normalizedKey.contains("accesskey")
                || normalizedKey.contains("authorization")
                || normalizedKey.contains("credential")
                || normalizedKey.contains("privatekey")
                || normalizedKey.contains("cookie")
                || normalizedKey.contains("session")
                || normalizedKey.contains("jwt")
                || normalizedKey.contains("bearer")
                || normalizedKey.contains("signature")
                || normalizedKey.endsWith("sig")) {
            return "[redacted]";
        }
        if (value instanceof java.util.Map<?, ?> nested) {
            java.util.Map<String, Object> redacted = new java.util.TreeMap<>();
            nested.forEach((nestedKey, nestedValue) -> {
                String name = String.valueOf(nestedKey);
                redacted.put(name, redactArgument(name, nestedValue));
            });
            return redacted;
        }
        if (value instanceof java.util.Collection<?> collection) {
            return collection.stream().map(item -> redactArgument("", item)).toList();
        }
        if (value instanceof String text) {
            return redactText(text);
        }
        return value;
    }

    private static String redactText(String text) {
        String redacted = URL_USER_INFO.matcher(text).replaceAll("$1[redacted]@");
        redacted = SENSITIVE_QUERY_PARAMETER.matcher(redacted).replaceAll("$1[redacted]");
        redacted = SHORT_SIGNATURE_QUERY_PARAMETER.matcher(redacted).replaceAll("$1[redacted]");
        redacted = COOKIE_HEADER.matcher(redacted).replaceAll("$1[redacted]");
        redacted = AUTHORIZATION_VALUE.matcher(redacted).replaceAll("$1[redacted]");
        redacted = BEARER_TOKEN.matcher(redacted).replaceAll("Bearer [redacted]");
        redacted = SENSITIVE_ASSIGNMENT.matcher(redacted).replaceAll("$1\"[redacted]\"");
        redacted = JWT_VALUE.matcher(redacted).replaceAll("[redacted]");
        return API_KEY_VALUE.matcher(redacted).replaceAll("[redacted]");
    }

    /**
     * 查询 工具 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.config("toolName", "string", "工具名称"),
                NodeInputParam.config("enableCache", "boolean", "是否启用缓存"),
                NodeInputParam.previous("toolResult", "object", "前序工具结果（工具链场景）"));
    }
}
