package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Defines the collaboration boundary for quota reservations and usage accounting.
 */
public interface QuotaGovernance {

    /**
     * Reserves quota for a request.
     *
     * @param actor actor and tenant requesting quota
     * @param request requested quota
     * @return whether the request is allowed and its reservation ID
     */
    QuotaDecision reserve(ActorContext actor, QuotaRequest request);

    /**
     * Reserves quota using a stable tenant-local reference. A retry using the same reference
     * returns the existing reservation while it remains active.
     *
     * @param actor actor and tenant requesting quota
     * @param request requested quota
     * @param referenceKey stable key such as a generation run ID
     * @return the existing or newly created reservation decision
     */
    default QuotaDecision reserve(ActorContext actor, QuotaRequest request, String referenceKey) {
        return reserve(actor, request);
    }

    /**
     * Renews a reservation that still represents live work.
     *
     * @param actor actor and tenant renewing quota
     * @param referenceKey stable key used during reservation
     * @return whether an active reservation was renewed
     */
    default boolean renew(ActorContext actor, String referenceKey) {
        return false;
    }

    /** Returns the configured reservation renewal interval. */
    default long reservationRenewalIntervalMillis() {
        return 60_000L;
    }

    /**
     * Settles the reservation identified by its numeric ID.
     *
     * @param actor actor and tenant settling quota
     * @param reservationId reservation ID
     * @param usage actual usage to account
     */
    void settle(ActorContext actor, long reservationId, QuotaUsage usage);

    /**
     * Settles the reservation identified by a stable tenant-local reference. Implementations may
     * accept a late settlement after reservation expiry so completed work can still be accounted;
     * repeated settlements must not account usage more than once.
     *
     * @param actor actor and tenant settling quota
     * @param referenceKey stable key used during reservation
     * @param usage actual usage to account
     */
    default void settle(ActorContext actor, String referenceKey, QuotaUsage usage) {
        throw new UnsupportedOperationException("Reference-based quota settlement is unsupported");
    }

    /**
     * Releases the reservation identified by its numeric ID.
     *
     * @param actor actor and tenant releasing quota
     * @param reservationId reservation ID
     */
    void release(ActorContext actor, long reservationId);

    /**
     * Releases the reservation identified by a stable tenant-local reference.
     *
     * @param actor actor and tenant releasing quota
     * @param referenceKey stable key used during reservation
     */
    default void release(ActorContext actor, String referenceKey) {
        throw new UnsupportedOperationException("Reference-based quota release is unsupported");
    }
}
