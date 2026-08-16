package com.shiyu.ai.agent.node.memory;

import com.shiyu.ai.agent.node.*;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import com.shiyu.ai.memory.magma.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Setter @Getter @Slf4j
public class MemoryRetrievalNode extends BaseNode {
    private MemoryRetrievalConfig config;
    private final MemoryQueryPort memoryService;
    private MemoryRetrievalNode(MemoryRetrievalConfig config, MemoryQueryPort service) { super(config != null ? config : new MemoryRetrievalConfig()); this.config = config != null ? config : new MemoryRetrievalConfig(); this.config.setNodeType(NodeType.MEMORY_RETRIEVAL); this.memoryService = service; }
    public static Builder builder() { return new Builder(); }
    public static class Builder { private MemoryRetrievalConfig config; private MemoryQueryPort memoryService; public Builder config(MemoryRetrievalConfig c){config=c;return this;} public Builder memoryService(MemoryQueryPort s){memoryService=s;return this;} public MemoryRetrievalNode build(){if(memoryService==null)throw new IllegalStateException("memory service is required");return new MemoryRetrievalNode(config,memoryService);} }
    @Override protected NodeOutput doExecute(NodeInput input) {
        String query = input.getParameter(FieldKey.QUERY, ""); Long userId = input.getParameter(FieldKey.USER_ID, 1L); int topK = input.getParameter(FieldKey.TOP_K, config.getTopK() == null ? 5 : config.getTopK());
        NodeOutput output = new NodeOutput();
        try {
            List<MemoryPath> paths = memoryService.retrieve(new MemoryQuery(userId == null ? 1L : userId, "agent", "USER", String.valueOf(userId == null ? "anonymous" : userId), query, Set.of(GraphType.SEMANTIC, GraphType.TEMPORAL, GraphType.CAUSAL, GraphType.ENTITY), null, null, 2, topK, 2000));
            List<Map<String,Object>> memories = paths.stream().map(p -> { Map<String,Object> m=new LinkedHashMap<>(); m.put("id",p.event().id()); m.put("content",p.event().content()); m.put("score",p.score()); m.put("sourceType",p.event().sourceType()); m.put("sourceId",p.event().sourceId()); m.put("relations",p.edges()); return m; }).toList();
            output.setSuccess(true); output.setMsg("memory retrieval completed"); output.addData(FieldKey.MEMORIES, memories); output.addData(FieldKey.MEMORY_COUNT, memories.size()); output.addData(FieldKey.MEMORY_CONTEXT, memories.stream().map(m -> String.valueOf(m.get("content"))).reduce("", (a,b)->a.isBlank()?b:a+"\n"+b)); return output;
        } catch(Exception e) { log.error("memory retrieval failed",e); output.setSuccess(false); output.setMsg(e.getMessage()); return output; }
    }
    @Override public List<NodeInputParam> getRequiredInputs(){ return List.of(NodeInputParam.apiRequired("query","string","query"), NodeInputParam.config("topK","number","maximum results")); }
}
