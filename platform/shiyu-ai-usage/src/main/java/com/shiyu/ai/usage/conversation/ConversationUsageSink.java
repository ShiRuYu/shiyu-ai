package com.shiyu.ai.usage.conversation;

import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.port.GenerationUsageSink;
import com.shiyu.ai.usage.service.UsageRecordService;
import org.springframework.stereotype.Component;

/** Converts the durable GenerationRun terminal state into the usage ledger. */
@Component
public class ConversationUsageSink implements GenerationUsageSink {
    private final UsageRecordService usage;

    public ConversationUsageSink(UsageRecordService usage) { this.usage = usage; }

    @Override
    public void completed(GenerationRun run) {
        usage.recordUsage(run.platform(), run.model(), safe(run.promptTokens()), safe(run.completionTokens()),
                run.latencyMs(), null, run.conversationId(), run.id());
    }

    private static int safe(long value) { return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value)); }
}
