package com.shiyu.ai.agent.cache;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class AgentLoaderDatabaseCoverageTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7L), new UserId(9L), false);

    @Test
    void loadsPublishedDefinitionAndSynchronizesRequiredInputs() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        NodeFactory factory = mock(NodeFactory.class);
        when(factory.createNode(any())).thenReturn(new RequiredNode());
        AgentDefBO definition = new AgentDefBO();
        definition.setAgentId("agent-1"); definition.setStatus(1); definition.setCurrentVersion("v1");
        definition.setName("Agent"); definition.setCreateTime(LocalDateTime.now()); definition.setUpdateTime(LocalDateTime.now());
        AgentVersionBO version = new AgentVersionBO();
        version.setVersionNumber("v1"); version.setDescription("Version"); version.setCreateTime(LocalDateTime.now());
        version.setGraphConfig("{\"name\":\"graph\",\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "agent-1")).thenReturn(definition);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "agent-1", "v1")).thenReturn(version);

        AgentDefinition loaded = new AgentLoader(factory, repository).loadFromDb(ACTOR, "agent-1");
        assertNotNull(loaded);
        assertEquals("v1", loaded.getCurrentVersion());
        assertNotNull(loaded.getVersion("v1"));
    }

    @Test
    void returnsNullForMalformedGraphAndNodeConstructionFailure() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        AgentDefBO definition = new AgentDefBO();
        definition.setStatus(1); definition.setCurrentVersion("v1");
        AgentVersionBO version = new AgentVersionBO(); version.setGraphConfig("not-json");
        when(repository.selectByAgentId(ACTOR.tenantId(), "agent-1")).thenReturn(definition);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "agent-1", "v1")).thenReturn(version);
        assertNull(new AgentLoader(mock(NodeFactory.class), repository).loadFromDb(ACTOR, "agent-1"));
    }

    @Test
    void reloadSkipsUnchangedInputMetadataAndHandlesExplicitGraphDefaults() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        NodeFactory factory = mock(NodeFactory.class);
        when(factory.createNode(any())).thenReturn(new RequiredNode());

        AgentDefBO definition = new AgentDefBO();
        definition.setAgentId("agent-1");
        definition.setStatus(1);
        definition.setCurrentVersion("v1");
        definition.setName("Agent");
        definition.setCreateTime(LocalDateTime.now());
        definition.setUpdateTime(LocalDateTime.now());
        AgentVersionBO version = new AgentVersionBO();
        version.setVersionNumber("v1");
        version.setCreateTime(LocalDateTime.now());
        version.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\",\"config\":{\"description\":\"custom\"}}},\"edges\":{},\"conditionalEdges\":{}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "agent-1")).thenReturn(definition);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "agent-1", "v1")).thenReturn(version);

        AgentLoader loader = new AgentLoader(factory, repository);
        assertNotNull(loader.loadFromDb(ACTOR, "agent-1"));
        assertNotNull(version.getExtInfo());
        assertNotNull(definition.getExtInfo());
        assertNotNull(loader.loadFromDb(ACTOR, "agent-1"));
        verify(repository, times(1)).updateVersion(ACTOR.tenantId(), version);
        verify(repository, times(1)).update(ACTOR.tenantId(), definition);
    }

    private static final class RequiredNode extends BaseNode {
        private RequiredNode() {
            super(NodeConfig.builder().nodeId("n").nodeName("n").build());
        }

        @Override
        protected NodeOutput doExecute(NodeInput input) {
            return NodeOutput.fromMap(Map.of());
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of(NodeInputParam.apiRequired("question", "string", "question"));
        }
    }
}
