package com.shiyu.ai.memory.implementation.persistence.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.memory.magma.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.core.type.TypeReference;

@Component
public class JdbcMagmaMemoryRepository implements MagmaMemoryRepository {
    private final JdbcTemplate jdbc;
    public JdbcMagmaMemoryRepository(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    public void insertEvent(MemoryEvent e) { jdbc.update("INSERT INTO MEMORY_EVENT (ID,TENANT_ID,NAMESPACE,SUBJECT_TYPE,SUBJECT_ID,EVENT_TYPE,CONTENT,OCCURRED_AT,SOURCE_TYPE,SOURCE_ID,ATTRIBUTES,CONFIDENCE,IMPORTANCE,STATUS,CONFIRMATION_POLICY,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", e.id(),e.tenantId().value(),e.namespace(),e.subjectType(),e.subjectId(),e.eventType(),e.content(),ts(e.occurredAt()),e.sourceType(),e.sourceId(),JSONUtils.toJsonString(e.attributes()),e.confidence(),e.importance(),e.status().name(),e.confirmationPolicy().name(),ts(e.createdAt()),ts(e.updatedAt())); }
    public Optional<MemoryEvent> findEvent(TenantId tenantId,String id) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND ID=?",this::mapEvent,tenantId.value(),id).stream().findFirst(); }
    public Optional<MemoryEvent> findLatestEvent(TenantId tenantId,String ns,String st,String sid) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS IN ('ACTIVE','CANDIDATE') ORDER BY OCCURRED_AT DESC, CREATED_AT DESC LIMIT 1",this::mapEvent,tenantId.value(),ns,st,sid).stream().findFirst(); }
    @Override public Optional<MemoryEvent> findPreviousEvent(TenantId tenantId,String ns,String st,String sid,Instant occurredAt) {
        return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS IN ('ACTIVE','CANDIDATE') AND OCCURRED_AT<=? ORDER BY OCCURRED_AT DESC, CREATED_AT DESC LIMIT 1",this::mapEvent,tenantId.value(),ns,st,sid,ts(occurredAt)).stream().findFirst();
    }
    @Override public Optional<MemoryEvent> findNextEvent(TenantId tenantId,String ns,String st,String sid,Instant occurredAt) {
        return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS IN ('ACTIVE','CANDIDATE') AND OCCURRED_AT>? ORDER BY OCCURRED_AT, CREATED_AT LIMIT 1",this::mapEvent,tenantId.value(),ns,st,sid,ts(occurredAt)).stream().findFirst();
    }
    public List<MemoryEvent> findCandidates(TenantId tenantId,String ns,String st,String sid,int limit) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS IN ('ACTIVE','CANDIDATE') ORDER BY OCCURRED_AT DESC LIMIT ?",this::mapEvent,tenantId.value(),ns,st,sid,Math.min(Math.max(limit,1),200)); }
    @Override public List<MemoryEvent> findByNamespace(TenantId tenantId,String ns,int limit) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND STATUS<>'REVOKED' ORDER BY OCCURRED_AT LIMIT ?",this::mapEvent,tenantId.value(),ns,Math.min(Math.max(limit,1),100000)); }
    public void updateEventStatus(TenantId tenantId,String id,MemoryEventStatus status) { jdbc.update("UPDATE MEMORY_EVENT SET STATUS=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE TENANT_ID=? AND ID=?",status.name(),tenantId.value(),id); }
    @Override public void deactivateEdgesForNode(TenantId tenantId, String nodeId) {
        jdbc.update("UPDATE MEMORY_EDGE SET ACTIVE=FALSE WHERE TENANT_ID=? AND (SOURCE_NODE_ID=? OR TARGET_NODE_ID=?)", tenantId.value(), nodeId, nodeId);
    }
    public void upsertEntity(MemoryEntity e) { jdbc.update("MERGE INTO MEMORY_ENTITY (ID,TENANT_ID,ENTITY_TYPE,EXTERNAL_REF,DISPLAY_NAME,NORMALIZED_NAME,ATTRIBUTES,ACTIVE) KEY(TENANT_ID,ENTITY_TYPE,EXTERNAL_REF) VALUES (?,?,?,?,?,?,?,?)",e.id(),e.tenantId().value(),e.entityType(),e.externalRef(),e.displayName(),e.normalizedName(),JSONUtils.toJsonString(e.attributes()),e.active()); }
    public void insertEdge(MemoryEdge e) { jdbc.update("INSERT INTO MEMORY_EDGE (ID,TENANT_ID,SOURCE_NODE_ID,TARGET_NODE_ID,GRAPH_TYPE,RELATION_TYPE,DIRECTED,WEIGHT,CONFIDENCE,ORIGIN,EVIDENCE_SOURCE,ACTIVE,CREATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",e.id(),e.tenantId().value(),e.sourceNodeId(),e.targetNodeId(),e.graphType().name(),e.relationType(),e.directed(),e.weight(),e.confidence(),e.origin().name(),e.evidenceSource(),e.active(),ts(e.createdAt())); }
    public List<MemoryEdge> findEdges(TenantId tenantId,String nodeId,GraphType graphType,int limit) { return jdbc.query("SELECT * FROM MEMORY_EDGE WHERE TENANT_ID=? AND (SOURCE_NODE_ID=? OR TARGET_NODE_ID=?) AND GRAPH_TYPE=? AND ACTIVE=TRUE ORDER BY CONFIDENCE DESC LIMIT ?",this::mapEdge,tenantId.value(),nodeId,nodeId,graphType.name(),Math.min(Math.max(limit,1),200)); }
    public void enqueueConsolidation(TenantId tenantId,String eventId) { Instant now=Instant.now(); jdbc.update("INSERT INTO MEMORY_CONSOLIDATION_JOB (TENANT_ID,EVENT_ID,STATUS,ATTEMPTS,AVAILABLE_AT,CREATED_AT,UPDATED_AT) VALUES (?,?, 'PENDING',0,?,?,?)",tenantId.value(),eventId,ts(now),ts(now),ts(now)); }
    @Override public void recordRetrievalTrace(MemoryRetrievalTrace trace) {
        Map<String, Double> weights = trace.graphWeights().entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        jdbc.update("INSERT INTO MEMORY_RETRIEVAL_TRACE (ID,TENANT_ID,NAMESPACE,QUERY_TEXT,ANCHOR_EVENT_IDS,GRAPH_WEIGHTS_JSON,RELATION_PATHS_JSON,FILTERED_EVENT_IDS,RESULT_EVENT_IDS,CREATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?)",
                trace.id(), trace.tenantId().value(), trace.namespace(), trace.queryText(), JSONUtils.toJsonString(trace.anchorEventIds()),
                JSONUtils.toJsonString(weights), JSONUtils.toJsonString(trace.relationPaths()), JSONUtils.toJsonString(trace.filteredEventIds()),
                JSONUtils.toJsonString(trace.resultEventIds()), ts(trace.createdAt()));
    }
    @Override public Optional<MemoryRetrievalTrace> findRetrievalTrace(TenantId tenantId,String id) {
        return jdbc.query("SELECT * FROM MEMORY_RETRIEVAL_TRACE WHERE TENANT_ID=? AND ID=?", (r,n)-> {
            List<String> anchors = parseList(r.getString("ANCHOR_EVENT_IDS"));
            Map<String,Object> rawWeights = parseMap(r.getString("GRAPH_WEIGHTS_JSON"));
            Map<GraphType,Double> weights = rawWeights.entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> GraphType.valueOf(e.getKey()), e -> ((Number)e.getValue()).doubleValue()));
            List<List<String>> paths = parsePaths(r.getString("RELATION_PATHS_JSON"));
            return new MemoryRetrievalTrace(r.getString("ID"),new TenantId(r.getLong("TENANT_ID")),r.getString("NAMESPACE"),r.getString("QUERY_TEXT"),anchors,
                    weights, paths, parseList(r.getString("FILTERED_EVENT_IDS")), parseList(r.getString("RESULT_EVENT_IDS")), r.getTimestamp("CREATED_AT").toInstant());
        }, tenantId.value(),id).stream().findFirst();
    }

    private MemoryEvent mapEvent(ResultSet r,int n)throws java.sql.SQLException { Map<String,Object> attrs=JSONUtils.parseMap(r.getString("ATTRIBUTES")); return new MemoryEvent(r.getString("ID"),new TenantId(r.getLong("TENANT_ID")),r.getString("NAMESPACE"),r.getString("SUBJECT_TYPE"),r.getString("SUBJECT_ID"),r.getString("EVENT_TYPE"),r.getString("CONTENT"),r.getTimestamp("OCCURRED_AT").toInstant(),r.getString("SOURCE_TYPE"),r.getString("SOURCE_ID"),attrs,r.getDouble("CONFIDENCE"),r.getDouble("IMPORTANCE"),MemoryEventStatus.valueOf(r.getString("STATUS")),ConfirmationPolicy.valueOf(r.getString("CONFIRMATION_POLICY")),r.getTimestamp("CREATED_AT").toInstant(),r.getTimestamp("UPDATED_AT").toInstant()); }
    private MemoryEdge mapEdge(ResultSet r,int n)throws java.sql.SQLException { return new MemoryEdge(r.getString("ID"),new TenantId(r.getLong("TENANT_ID")),r.getString("SOURCE_NODE_ID"),r.getString("TARGET_NODE_ID"),GraphType.valueOf(r.getString("GRAPH_TYPE")),r.getString("RELATION_TYPE"),r.getBoolean("DIRECTED"),r.getDouble("WEIGHT"),r.getDouble("CONFIDENCE"),EdgeOrigin.valueOf(r.getString("ORIGIN")),r.getString("EVIDENCE_SOURCE"),r.getBoolean("ACTIVE"),r.getTimestamp("CREATED_AT").toInstant()); }
    private List<String> parseList(String value) { return value == null || value.isBlank() ? List.of() : JSONUtils.parseObject(value, new TypeReference<List<String>>() { }); }
    private Map<String,Object> parseMap(String value) { return value == null || value.isBlank() ? Map.of() : JSONUtils.parseObject(value, new TypeReference<Map<String, Object>>() { }); }
    private List<List<String>> parsePaths(String value) { return value == null || value.isBlank() ? List.of() : JSONUtils.parseObject(value, new TypeReference<List<List<String>>>() { }); }
    private static Timestamp ts(Instant i){return Timestamp.from(i==null?Instant.now():i);}
}

