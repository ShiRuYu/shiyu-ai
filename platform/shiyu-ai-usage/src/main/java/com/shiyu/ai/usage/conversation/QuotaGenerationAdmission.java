package com.shiyu.ai.usage.conversation;

import com.shiyu.ai.conversation.GenerationAdmissionException;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.usage.port.QuotaGateway;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Connects the local quota implementation to the GenerationRun lifecycle. */
@Component
public class QuotaGenerationAdmission implements GenerationAdmission {
    private final QuotaGateway quota;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    public QuotaGenerationAdmission(QuotaGateway quota) { this.quota = quota; }

    @Override
    public void reserve(long tenantId, GenerationRun run, int estimatedPromptTokens) {
        QuotaGateway.Decision decision = quota.reserve(tenantId, Math.max(0, estimatedPromptTokens), 0);
        if (!decision.allowed()) throw new GenerationAdmissionException(decision.errorCode());
        reservations.put(run.id(), new Reservation(decision.reservationId(), Math.max(0, estimatedPromptTokens)));
    }

    @Override
    public void settle(long tenantId, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null) quota.settle(tenantId, reservation.id(), safe(run.promptTokens()) > 0 ? safe(run.promptTokens()) : reservation.estimatedPromptTokens(), safe(run.completionTokens()));
    }

    @Override
    public void release(long tenantId, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null) quota.release(tenantId, reservation.id());
    }

    private static int safe(long value) { return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value)); }
    private record Reservation(long id, int estimatedPromptTokens) { }
}
