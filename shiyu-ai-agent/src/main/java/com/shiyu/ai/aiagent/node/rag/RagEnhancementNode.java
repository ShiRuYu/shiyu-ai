package com.shiyu.ai.aiagent.node.rag;

import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 增强节点
 * <p>
 * 对检索到的文档执行增强处理，支持三种策略：
 * <ul>
 *   <li><b>SUMMARIZATION</b> — 对文档进行摘要合并，控制输出长度</li>
 *   <li><b>RE_RANK</b> — 根据分数重新排序，并结合 contextWindowSize 截取</li>
 *   <li><b>FILTER</b> — 按相似度阈值过滤低分文档</li>
 * </ul>
 * 处理后的上下文内容写入 FieldKey#CONTEXT 供后续 LLM 使用。
 * <p>
 * 所有配置参数优先从 input (AgentState) 读取，未提供时回退到 config 默认值。
 */
@Setter
@Getter
@Slf4j
public class RagEnhancementNode extends BaseNode {

    private RagEnhancementConfig config;

    private RagEnhancementNode(RagEnhancementConfig config) {
        super(config != null ? config : new RagEnhancementConfig());
        this.config = config != null ? config : new RagEnhancementConfig();
        this.config.setNodeType(NodeType.RAG_ENHANCEMENT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RagEnhancementConfig config;

        public Builder config(RagEnhancementConfig config) {
            this.config = config;
            return this;
        }

        public RagEnhancementNode build() {
            return new RagEnhancementNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 RAG 增强节点: {}", config.getNodeName());
        log.debug("增强配置: strategy={}, addContext={}, contextWindowSize={}, maxLength={}",
                config.getEnhancementStrategy(), config.getAddContext(),
                config.getContextWindowSize(), config.getMaxLength());

        try {
            // 1. 从输入中提取文档列表
            List<Map<String, Object>> documents = input.getParameter(
                    FieldKey.DOCUMENTS, Collections.emptyList());
            String originalContext = input.getParameter(FieldKey.CONTEXT, "");

            if (documents == null || documents.isEmpty()) {
                log.warn("RAG 增强节点: 输入文档列表为空，跳过增强");
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.setMsg("RAG 增强跳过: 无输入文档");
                Boolean addCtx = input.getParameter(FieldKey.ADD_CONTEXT,
                        config.getAddContext() != null && config.getAddContext());
                if (addCtx != null && addCtx) {
                    output.addData(FieldKey.CONTEXT, originalContext);
                }
                return output;
            }

            // 2. 读取配置参数（优先从 input，回退到 config）
            String strategy = input.getParameter(FieldKey.ENHANCEMENT_STRATEGY,
                    config.getEnhancementStrategy());
            if (strategy == null) {
                strategy = "SUMMARIZATION";
            }
            strategy = strategy.toUpperCase();

            int windowSize = input.getParameter(FieldKey.CONTEXT_WINDOW_SIZE,
                    config.getContextWindowSize() != null ? config.getContextWindowSize() : 3);
            int maxLen = input.getParameter(FieldKey.MAX_LENGTH,
                    config.getMaxLength() != null ? config.getMaxLength() : 2000);
            double threshold = input.getParameter(FieldKey.SIMILARITY_THRESHOLD,
                    config.getSimilarityThreshold() != null ? config.getSimilarityThreshold() : 0.5);
            Boolean addContext = input.getParameter(FieldKey.ADD_CONTEXT,
                    config.getAddContext() != null && config.getAddContext());

            // 3. 执行增强策略
            EnhancedResult enhanced = switch (strategy) {
                case "RE_RANK" -> reRank(documents, windowSize, maxLen);
                case "FILTER" -> filter(documents, threshold, maxLen);
                default -> summarize(documents, windowSize, maxLen);
            };

            log.info("增强策略 [{}]: 输入 {} 篇文档, 输出 {} 篇, 上下文长度 {} 字符",
                    strategy, documents.size(), enhanced.documents().size(), enhanced.context().length());

            // 4. 构建输出
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("RAG 增强成功 (策略: " + strategy + ")");

            if (addContext != null && addContext) {
                output.addData(FieldKey.CONTEXT, enhanced.context());
            }
            output.addData(FieldKey.ENHANCED_DOCUMENTS, enhanced.documents());
            output.addData(FieldKey.ENHANCED_COUNT, enhanced.documents().size());
            output.addData(FieldKey.ENHANCEMENT_STRATEGY, strategy);

            return output;

        } catch (Exception e) {
            log.error("RAG 增强节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("RAG 增强失败: " + e.getMessage());
            return output;
        }
    }

    // ======================== 增强策略 ========================

    /** SUMMARIZATION: 摘要合并 + 上下文窗口截断 */
    private EnhancedResult summarize(List<Map<String, Object>> documents, int windowSize, int maxLen) {
        List<Map<String, Object>> topDocs = sortByScore(documents).stream()
                .limit(Math.max(1, windowSize))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("以下是检索到的相关文档摘要：\n\n");
        for (int i = 0; i < topDocs.size(); i++) {
            Map<String, Object> doc = topDocs.get(i);
            String content = strField(doc, "content", "");
            double score = numField(doc, "score", 0.0);
            sb.append("【文档 ").append(i + 1).append("】(相关度: ")
                    .append(String.format("%.2f", score)).append(")\n");
            if (content.length() > maxLen / Math.max(1, topDocs.size())) {
                content = content.substring(0, maxLen / Math.max(1, topDocs.size())) + "...(截断)";
            }
            sb.append(content).append("\n\n");
        }

        String context = sb.toString();
        if (context.length() > maxLen) {
            context = context.substring(0, maxLen) + "\n...(上下文截断)";
        }
        return new EnhancedResult(topDocs, context);
    }

    /** RE_RANK: 按分数重排序 + 上下文窗口 */
    private EnhancedResult reRank(List<Map<String, Object>> documents, int windowSize, int maxLen) {
        List<Map<String, Object>> sorted = sortByScore(documents);
        List<Map<String, Object>> topDocs = sorted.stream()
                .limit(Math.max(1, windowSize))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("重排序后的相关文档（按相关度降序）：\n\n");
        for (int i = 0; i < topDocs.size(); i++) {
            Map<String, Object> doc = topDocs.get(i);
            String content = strField(doc, "content", "");
            double score = numField(doc, "score", 0.0);
            sb.append("[").append(i + 1).append("] score=")
                    .append(String.format("%.2f", score)).append("\n")
                    .append(content).append("\n\n");
        }

        String context = sb.toString();
        if (context.length() > maxLen) {
            context = context.substring(0, maxLen) + "\n...(上下文截断)";
        }
        return new EnhancedResult(topDocs, context);
    }

    /** FILTER: 按相似度阈值过滤 + 分数排序 */
    private EnhancedResult filter(List<Map<String, Object>> documents, double threshold, int maxLen) {
        List<Map<String, Object>> filtered = sortByScore(documents).stream()
                .filter(doc -> numField(doc, "score", 0.0) >= threshold)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("过滤后的相关文档（阈值: ").append(String.format("%.2f", threshold)).append("）：\n\n");
        for (int i = 0; i < filtered.size(); i++) {
            Map<String, Object> doc = filtered.get(i);
            String content = strField(doc, "content", "");
            double score = numField(doc, "score", 0.0);
            sb.append("【文档 ").append(i + 1).append("】(score=")
                    .append(String.format("%.2f", score)).append(")\n")
                    .append(content).append("\n\n");
        }

        String context = sb.toString();
        if (context.length() > maxLen) {
            context = context.substring(0, maxLen) + "\n...(上下文截断)";
        }
        return new EnhancedResult(filtered, context);
    }

    // ======================== 工具方法 ========================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> sortByScore(List<Map<String, Object>> documents) {
        List<Map<String, Object>> sorted = new ArrayList<>(documents);
        sorted.sort((a, b) -> {
            double sa = numField(a, "score", 0.0);
            double sb = numField(b, "score", 0.0);
            return Double.compare(sb, sa);
        });
        return sorted;
    }

    private String strField(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v != null ? v.toString() : def;
    }

    private double numField(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    /** 增强结果 */
    private record EnhancedResult(List<Map<String, Object>> documents, String context) {}
}
