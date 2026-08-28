package com.shiyu.ai.application.governance;

import com.shiyu.ai.conversation.GenerationAdmissionException;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.governance.contract.QuotaDecision;
import com.shiyu.ai.governance.contract.QuotaGovernance;
import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Connects the local quota implementation to the GenerationRun lifecycle. */
@Component
public class QuotaGenerationAdmission implements GenerationAdmission {
    private final QuotaGovernance quota;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    public QuotaGenerationAdmission(QuotaGovernance quota) { this.quota = quota; }

    @Override
    public void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) {
        QuotaDecision decision = quota.reserve(actor, new QuotaRequest(Math.max(0, estimatedPromptTokens), 0));
        if (!decision.allowed()) throw new GenerationAdmissionException(decision.errorCode());
        reservations.put(run.id(), new Reservation(decision.reservationId(), Math.max(0, estimatedPromptTokens)));
    }

    @Override
    public void settle(ActorContext actor, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null) quota.settle(actor, reservation.id(), new QuotaUsage(
                safe(run.promptTokens()) > 0 ? safe(run.promptTokens()) : reservation.estimatedPromptTokens(),
                safe(run.completionTokens())));
    }

    @Override
    public void release(ActorContext actor, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null) quota.release(actor, reservation.id());
    }

    private static int safe(long value) { return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value)); }
    private record Reservation(long id, int estimatedPromptTokens) { }
}
