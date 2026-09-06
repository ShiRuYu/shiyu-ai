package com.shiyu.ai.runtime;

import java.util.List;

public interface ContextRetrievalPort {
    List<ContextItem> retrieve(ContextQuery query);
}
