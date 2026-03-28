package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG 检索节点
 * 用于从知识库中检索相关信息
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class RagRetrievalNode extends BaseNode {

    private RagRetrievalConfig config;

    public RagRetrievalNode() {
        this.config = new RagRetrievalConfig();
        // 设置节点类型为 RAG_RETRIEVAL
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
    }

    public RagRetrievalNode(RagRetrievalConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 RAG_RETRIEVAL
        this.config.setNodeType(NodeType.RAG_RETRIEVAL);
    }

    public void setRagRetrievalConfig(RagRetrievalConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 RAG 检索节点：{}", config.getNodeName());
        log.debug("检索配置：knowledgeBaseId={}, topK={}, strategy={}", 
                config.getKnowledgeBaseId(), config.getTopK(), config.getRetrievalStrategy());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("RAG 检索节点执行成功");
            
            // TODO: 实现具体的 RAG 检索逻辑
            
            log.info("RAG 检索节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("RAG 检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("RAG 检索节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
