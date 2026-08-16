package com.shiyu.ai.dal.memory.service;

import com.shiyu.ai.common.core.utils.JSONUtils;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/** Local H2 consolidation worker. It leases jobs so a restart can resume pending work. */
@Component
public class MagmaConsolidationWorker {
    private final JdbcTemplate jdbc;
    public MagmaConsolidationWorker(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    @Scheduled(fixedDelayString = "${shiyu.memory.consolidation-delay-ms:3000}")
    public void processBatch() {
        List<Long> ids = jdbc.query("SELECT ID FROM MEMORY_CONSOLIDATION_JOB WHERE STATUS='PENDING' AND AVAILABLE_AT<=CURRENT_TIMESTAMP AND (LEASED_UNTIL IS NULL OR LEASED_UNTIL<CURRENT_TIMESTAMP) ORDER BY ID LIMIT 10", (r,n)->r.getLong(1));
        for (Long id : ids) {
            int claimed = jdbc.update("UPDATE MEMORY_CONSOLIDATION_JOB SET STATUS='RUNNING',LEASED_UNTIL=?,UPDATED_AT=CURRENT_TIMESTAMP,ATTEMPTS=ATTEMPTS+1 WHERE ID=? AND STATUS='PENDING'", Timestamp.from(Instant.now().plusSeconds(60)), id);
            if (claimed == 0) continue;
            try {
                consolidate(id);
                jdbc.update("UPDATE MEMORY_CONSOLIDATION_JOB SET STATUS='COMPLETED',LEASED_UNTIL=NULL,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=?", id);
            } catch (RuntimeException failure) {
                jdbc.update("UPDATE MEMORY_CONSOLIDATION_JOB SET STATUS=CASE WHEN ATTEMPTS>=5 THEN 'FAILED' ELSE 'PENDING' END,LEASED_UNTIL=NULL,AVAILABLE_AT=DATEADD('SECOND', POWER(2, ATTEMPTS), CURRENT_TIMESTAMP),LAST_ERROR=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=?", failure.getMessage(), id);
            }
        }
    }

    /** Builds deterministic structure without making the fast ingestion path wait for a model. */
    private void consolidate(long jobId) {
        Map<String, Object> job = jdbc.queryForMap("SELECT TENANT_ID,EVENT_ID FROM MEMORY_CONSOLIDATION_JOB WHERE ID=?", jobId);
        long tenant = ((Number) job.get("TENANT_ID")).longValue();
        String eventId = String.valueOf(job.get("EVENT_ID"));
        Map<String, Object> event = jdbc.queryForMap("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND ID=?", tenant, eventId);
        if ("REVOKED".equalsIgnoreCase(String.valueOf(event.get("STATUS")))
                || "SUPERSEDED".equalsIgnoreCase(String.valueOf(event.get("STATUS")))) {
            return;
        }
        String entityId = UUID.nameUUIDFromBytes((tenant + ":" + event.get("SUBJECT_TYPE") + ":" + event.get("SUBJECT_ID")).getBytes()).toString();
        String entityType = String.valueOf(event.get("SUBJECT_TYPE"));
        String externalRef = String.valueOf(event.get("SUBJECT_ID"));
        String displayName = String.valueOf(event.get("SUBJECT_ID"));
        jdbc.update("MERGE INTO MEMORY_ENTITY (ID,TENANT_ID,ENTITY_TYPE,EXTERNAL_REF,DISPLAY_NAME,NORMALIZED_NAME,ATTRIBUTES,ACTIVE) KEY(TENANT_ID,ENTITY_TYPE,EXTERNAL_REF) VALUES (?,?,?,?,?,?,?,TRUE)",
                entityId, tenant, entityType, externalRef, displayName, displayName.toLowerCase(), "{}");
        edgeIfAbsent(tenant, eventId, entityId, "ENTITY", "about", 1.0, 1.0, "RULE", eventId);

        String content = String.valueOf(event.get("CONTENT"));
        String namespace = String.valueOf(event.get("NAMESPACE"));
        jdbc.query("SELECT ID,CONTENT FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND STATUS<>'REVOKED' AND ID<>? ORDER BY OCCURRED_AT DESC LIMIT 100",
                (rs, row) -> {
                    double similarity = jaccard(content, rs.getString("CONTENT"));
                    if (similarity >= 0.20d) edgeIfAbsent(tenant, eventId, rs.getString("ID"), "SEMANTIC", "similar", similarity, similarity, "RULE", eventId);
                    return null;
                }, tenant, namespace, eventId);

        Map<String, Object> attributes = JSONUtils.parseObject((String) event.get("ATTRIBUTES"), Map.class);
        Object raw = attributes == null ? null : attributes.get("causesEventId");
        if (raw != null && !String.valueOf(raw).isBlank()) {
            String caused = String.valueOf(raw);
            Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM MEMORY_EVENT WHERE TENANT_ID=? AND ID=?", Integer.class, tenant, caused);
            if (exists != null && exists > 0) edgeIfAbsent(tenant, caused, eventId, "CAUSAL", "causes", 1.0, 0.8, "DOMAIN", eventId);
        }
    }

    private void edgeIfAbsent(long tenant, String source, String target, String graph, String relation,
                               double weight, double confidence, String origin, String evidence) {
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM MEMORY_EDGE WHERE TENANT_ID=? AND SOURCE_NODE_ID=? AND TARGET_NODE_ID=? AND GRAPH_TYPE=? AND RELATION_TYPE=? AND ACTIVE=TRUE",
                Integer.class, tenant, source, target, graph, relation);
        if (exists != null && exists > 0) return;
        jdbc.update("INSERT INTO MEMORY_EDGE (ID,TENANT_ID,SOURCE_NODE_ID,TARGET_NODE_ID,GRAPH_TYPE,RELATION_TYPE,DIRECTED,WEIGHT,CONFIDENCE,ORIGIN,EVIDENCE_SOURCE,ACTIVE,CREATED_AT) VALUES (?,?,?,?,?,?,TRUE,?,?,?,?,TRUE,CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(), tenant, source, target, graph, relation, weight, confidence, origin, evidence);
    }

    private static double jaccard(String left, String right) {
        Set<String> a = tokens(left), b = tokens(right);
        if (a.isEmpty() || b.isEmpty()) return 0d;
        Set<String> union = new HashSet<>(a); union.addAll(b);
        Set<String> intersection = new HashSet<>(a); intersection.retainAll(b);
        return (double) intersection.size() / union.size();
    }
    private static Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        for (String token : (text == null ? "" : text.toLowerCase()).split("[^\\p{L}\\p{Nd}]+")) if (token.length() >= 2) result.add(token);
        return result;
    }
}
