package com.shiyu.ai.agent.contract.runtime;

import java.util.List;

/** Provider-neutral retrieval port used by agent context assembly. */
public interface ContextRetrievalPort {
    /** Retrieves ranked context items for one validated query. */
    List<ContextItem> retrieve(ContextQuery query);
}
