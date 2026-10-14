package com.shiyu.ai.agent.implementation.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BaseNodeTenantScopeTest {
    @Test
    void timedNodeKeepsTenantScopeInWorkerThread() throws Exception {
        BaseNode node = new BaseNode(NodeConfig.builder().timeout(1000L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                return NodeOutput.builder().success(true)
                        .data(Map.of("tenantId", TenantScope.require().value())).build();
            }
        };
        TenantScope.set(new TenantId(7L));
        try {
            assertEquals(7L, node.apply(Map.of()).get("tenantId"));
        } finally {
            TenantScope.clear();
        }
    }
}
