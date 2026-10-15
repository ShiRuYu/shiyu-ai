package com.shiyu.ai.agent.implementation.runtime.port;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.kernel.context.ActorContext;

/** 审批通过的 Agent 执行恢复后，持久化对应的会话结果。 */
public interface AgentConversationCompletionPort {
    /**
     * 将获批后恢复的 Agent 执行结果写入关联会话。
     *
     * @param actor 执行所属用户及租户上下文
     * @param run 关联会话的运行记录
     * @param execution 已结束的 Agent 执行
     */
    void completeApprovedRun(ActorContext actor, AiRun run, Execution execution);
}
