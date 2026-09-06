package com.shiyu.ai.agent.builder;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.service.AgentService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentBuilderTest {
    @Test
    void buildsGraphWithEdgesAndRegistersDefinition() {
        BaseNode start = node("start");
        BaseNode end = node("end");
        AgentService service = mock(AgentService.class);
        AgentBuilder builder = new AgentBuilder()
                .agentId("demo")
                .name("Demo")
                .description("demo agent")
                .version("v2")
                .versionDescription("version")
                .addNode("start", start)
                .addNode("end", end)
                .setStartNode("start")
                .setEndNode("end")
                .addEdge("start", "end")
                .addConditionalEdge("start", "end", Map.of(value -> Boolean.TRUE.equals(value.get("ok")), "end"));

        AgentDefinition definition = builder.build();
        assertEquals("demo", definition.getAgentId());
        assertEquals("v2", definition.getCurrentVersion());
        assertEquals(2, definition.getVersion("v2").getGraph().getNodes().size());
        assertEquals("demo", builder.buildAndRegister(service).getAgentId());
        verify(service).registerSystemAgent(any(AgentDefinition.class));
    }

    @Test
    void rejectsIncompleteDefinitionsAndWrapsRegistrationFailures() {
        assertThrows(RuntimeException.class, () -> new AgentBuilder().build());
        assertThrows(RuntimeException.class, () -> new AgentBuilder().name("n").setStartNode("s").addNode("s", node("s")).build());
        assertThrows(RuntimeException.class, () -> new AgentBuilder().agentId("a").setStartNode("s").addNode("s", node("s")).build());

        AgentService service = mock(AgentService.class);
        doThrow(new IllegalStateException("blocked")).when(service).registerSystemAgent(any());
        AgentBuilder valid = new AgentBuilder().agentId("a").name("A")
                .addNode("s", node("s")).setStartNode("s");
        assertThrows(RuntimeException.class, () -> valid.buildAndRegister(service));
    }

    private static BaseNode node(String id) {
        return new BaseNode(NodeConfig.builder().nodeId(id).nodeName(id).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                return NodeOutput.fromMap(Map.of("ok", true));
            }
        };
    }
}
