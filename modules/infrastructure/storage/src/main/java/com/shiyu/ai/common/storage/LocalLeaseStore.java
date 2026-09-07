package com.shiyu.ai.common.storage;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalLeaseStore implements LeaseStore {
    private final ConcurrentHashMap<String, Lease> leases = new ConcurrentHashMap<>();
    public boolean tryAcquire(String key, String owner, Duration ttl) {
        long expires = System.nanoTime() + ttl.toNanos();
        return leases.compute(key, (k, old) -> old == null || old.expiresAt < System.nanoTime() ? new Lease(owner, expires) : old).owner.equals(owner);
    }
    public boolean renew(String key, String owner, Duration ttl) { return leases.computeIfPresent(key, (k, old) -> old.owner.equals(owner) ? new Lease(owner, System.nanoTime() + ttl.toNanos()) : old) != null && owner.equals(leases.get(key).owner); }
    public void release(String key, String owner) { leases.computeIfPresent(key, (k, old) -> old.owner.equals(owner) ? null : old); }
    private record Lease(String owner, long expiresAt) { }
}
