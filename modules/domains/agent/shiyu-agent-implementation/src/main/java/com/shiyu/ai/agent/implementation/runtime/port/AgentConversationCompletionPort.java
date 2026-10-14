package com.shiyu.ai.agent.implementation.runtime.port;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.kernel.context.ActorContext;

/** Persists the conversation outcome after an approved Agent execution resumes. */
public interface AgentConversationCompletionPort {
    void completeApprovedRun(ActorContext actor, AiRun run, Execution execution);
}
