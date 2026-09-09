package com.shiyu.ai.agent.contract.runtime;

import java.util.List;

public interface ContextRetrievalPort {
    List<ContextItem> retrieve(ContextQuery query);
}
