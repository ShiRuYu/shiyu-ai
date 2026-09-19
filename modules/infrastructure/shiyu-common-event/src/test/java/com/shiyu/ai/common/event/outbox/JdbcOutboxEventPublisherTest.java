package com.shiyu.ai.common.event.outbox;

import com.shiyu.ai.common.event.config.EventInfrastructureProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.event.DomainEvent;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

class JdbcOutboxEventPublisherTest {

    @org.junit.jupiter.api.AfterEach
    void clearTenantScope() {
        TenantScope.clear();
    }

    @Test
    void storesEnvelopeInOutboxWithoutExternalBroker() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:outbox;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        JdbcOutboxEventPublisher publisher =
                new JdbcOutboxEventPublisher(jdbc, new EventInfrastructureProperties());

        TenantScope.withTenant(
                new TenantId(1L),
                () -> {
                    publisher.publish(
                            new DomainEventEnvelope<>(
                                    new TenantId(1L),
                                    new UserId(2L),
                                    new CorrelationId("corr-1"),
                                    Instant.now(),
                                    new TestEvent("TEST_EVENT")));
                    return null;
                });

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM shiyu_event_outbox", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT EVENT_TYPE FROM shiyu_event_outbox", String.class))
                .isEqualTo("TEST_EVENT");
    }

    @Test
    void rejectsEnvelopeOutsideTheBoundTenant() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:outbox-mismatch;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        JdbcOutboxEventPublisher publisher =
                new JdbcOutboxEventPublisher(jdbc, new EventInfrastructureProperties());
        TenantScope.set(new TenantId(7L));

        assertThatThrownBy(
                        () ->
                                publisher.publish(
                                        new DomainEventEnvelope<>(
                                                new TenantId(9L),
                                                new UserId(2L),
                                                new CorrelationId("corr-2"),
                                                Instant.now(),
                                                new TestEvent("TEST_EVENT"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private record TestEvent(String eventType) implements DomainEvent {}
}
