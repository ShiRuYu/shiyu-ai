package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.domain.model.NodeExecutionBO;
import com.shiyu.ai.agent.persistence.dataobject.AgentCheckpointDO;
import com.shiyu.ai.agent.persistence.dataobject.AgentExecutionDO;
import com.shiyu.ai.agent.persistence.dataobject.NodeExecutionDO;
import com.shiyu.ai.agent.persistence.mapper.AgentCheckpointMapper;
import com.shiyu.ai.agent.persistence.mapper.AgentExecutionMapper;
import com.shiyu.ai.agent.persistence.mapper.NodeExecutionMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentExecutionRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(7);

    @Test
    void persistsAndQueriesAgentExecutionsAndNodesWithTenantScope() throws Exception {
        AgentExecutionMapper executionMapper = mock(AgentExecutionMapper.class);
        NodeExecutionMapper nodeMapper = mock(NodeExecutionMapper.class);
        AgentExecutionRepositoryImpl executions = new AgentExecutionRepositoryImpl();
        NodeExecutionRepositoryImpl nodes = new NodeExecutionRepositoryImpl();
        inject(executions, "agentExecutionMapper", executionMapper);
        inject(nodes, "nodeExecutionMapper", nodeMapper);

        AgentExecutionBO execution = new AgentExecutionBO();
        AgentExecutionDO executionData = new AgentExecutionDO();
        executionData.setId(11L);
        AgentExecutionBO executionResult = new AgentExecutionBO();
        NodeExecutionBO node = new NodeExecutionBO();
        NodeExecutionDO nodeData = new NodeExecutionDO();
        nodeData.setId(12L);
        NodeExecutionBO nodeResult = new NodeExecutionBO();
        when(executionMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(executionData);
        when(executionMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(executionData));
        when(nodeMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(nodeData));
        doAnswer(invocation -> { ((AgentExecutionDO) invocation.getArgument(0)).setId(11L); return 1; })
                .when(executionMapper).insertSelective(any(AgentExecutionDO.class));
        doAnswer(invocation -> { ((NodeExecutionDO) invocation.getArgument(0)).setId(12L); return 1; })
                .when(nodeMapper).insertSelective(any(NodeExecutionDO.class));

        try (var conversion = mockStatic(MapstructUtils.class)) {
            conversion.when(() -> MapstructUtils.convert(any(AgentExecutionBO.class), eq(AgentExecutionDO.class)))
                    .thenReturn(executionData);
            conversion.when(() -> MapstructUtils.convert(any(AgentExecutionDO.class), eq(AgentExecutionBO.class)))
                    .thenReturn(executionResult);
            conversion.when(() -> MapstructUtils.convert(anyList(), eq(AgentExecutionBO.class)))
                    .thenReturn(List.of(executionResult));
            conversion.when(() -> MapstructUtils.convert(any(NodeExecutionBO.class), eq(NodeExecutionDO.class)))
                    .thenReturn(nodeData);
            conversion.when(() -> MapstructUtils.convert(any(NodeExecutionDO.class), eq(NodeExecutionBO.class)))
                    .thenReturn(nodeResult);
            conversion.when(() -> MapstructUtils.convert(anyList(), eq(NodeExecutionBO.class)))
                    .thenReturn(List.of(nodeResult));

            executions.insert(TENANT, execution);
            executions.update(TENANT, execution);
            assertSame(executionResult, executions.selectByExecutionId(TENANT, "execution"));
            assertEquals(1, executions.selectBySessionId(TENANT, "session").size());
            assertEquals(1, executions.selectByAgentId(TENANT, "agent", 0).size());

            nodes.insert(TENANT, node);
            nodes.update(TENANT, node);
            assertEquals(1, nodes.selectByExecutionId(TENANT, "execution").size());
            verify(executionMapper).update(any(AgentExecutionDO.class));
            verify(nodeMapper).updateByQuery(any(NodeExecutionDO.class), any(QueryWrapper.class));
        }
    }

    @Test
    void checkpointsEnforceTenantAndSupportLifecycleOperations() throws Exception {
        AgentCheckpointMapper mapper = mock(AgentCheckpointMapper.class);
        AgentCheckpointRepositoryImpl repository = new AgentCheckpointRepositoryImpl();
        inject(repository, "agentCheckpointMapper", mapper);
        AgentCheckpointBO checkpoint = new AgentCheckpointBO();
        AgentCheckpointDO data = new AgentCheckpointDO();
        data.setId(3L);
        AgentCheckpointBO mapped = new AgentCheckpointBO();
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(data);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(data));
        doAnswer(invocation -> { ((AgentCheckpointDO) invocation.getArgument(0)).setId(3L); return 1; })
                .when(mapper).insertSelective(any(AgentCheckpointDO.class));
        try (var conversion = mockStatic(MapstructUtils.class)) {
            conversion.when(() -> MapstructUtils.convert(any(AgentCheckpointBO.class), eq(AgentCheckpointDO.class)))
                    .thenReturn(data);
            conversion.when(() -> MapstructUtils.convert(any(AgentCheckpointDO.class), eq(AgentCheckpointBO.class)))
                    .thenReturn(mapped);
            conversion.when(() -> MapstructUtils.convert(anyList(), eq(AgentCheckpointBO.class)))
                    .thenReturn(List.of(mapped));
            repository.insert(TENANT, checkpoint);
            assertEquals(3L, checkpoint.getId());
            assertSame(mapped, repository.selectByCheckpointId(TENANT, "checkpoint"));
            assertSame(mapped, repository.selectLatestByExecutionId(TENANT, "execution"));
            assertEquals(1, repository.listByExecutionId(TENANT, "execution").size());
            repository.deleteByCheckpointId(TENANT, "checkpoint");
            repository.deleteByExecutionId(TENANT, "execution");
            verify(mapper, times(2)).deleteByQuery(any(QueryWrapper.class));
            assertThrows(IllegalArgumentException.class, () -> repository.insert(null, checkpoint));
            assertThrows(IllegalArgumentException.class, () -> repository.insert(TENANT, null));
            assertThrows(IllegalArgumentException.class, () -> repository.listByExecutionId(null, "execution"));
        }
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
