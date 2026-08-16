package com.shiyu.ai.dal.memory.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.memory.magma.*;
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

@Component
public class JdbcMagmaMemoryRepository implements MagmaMemoryRepository {
    private final JdbcTemplate jdbc;
    public JdbcMagmaMemoryRepository(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    public void insertEvent(MemoryEvent e) { jdbc.update("INSERT INTO MEMORY_EVENT (ID,TENANT_ID,NAMESPACE,SUBJECT_TYPE,SUBJECT_ID,EVENT_TYPE,CONTENT,OCCURRED_AT,SOURCE_TYPE,SOURCE_ID,ATTRIBUTES,CONFIDENCE,IMPORTANCE,STATUS,CONFIRMATION_POLICY,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", e.id(),e.tenantId(),e.namespace(),e.subjectType(),e.subjectId(),e.eventType(),e.content(),ts(e.occurredAt()),e.sourceType(),e.sourceId(),JSONUtils.toJsonString(e.attributes()),e.confidence(),e.importance(),e.status().name(),e.confirmationPolicy().name(),ts(e.createdAt()),ts(e.updatedAt())); }
    public Optional<MemoryEvent> findEvent(long tenantId,String id) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND ID=?",this::mapEvent,tenantId,id).stream().findFirst(); }
    public Optional<MemoryEvent> findLatestEvent(long tenantId,String ns,String st,String sid) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS<>'REVOKED' ORDER BY OCCURRED_AT DESC, CREATED_AT DESC LIMIT 1",this::mapEvent,tenantId,ns,st,sid).stream().findFirst(); }
    public List<MemoryEvent> findCandidates(long tenantId,String ns,String st,String sid,int limit) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND SUBJECT_TYPE=? AND SUBJECT_ID=? AND STATUS IN ('ACTIVE','CANDIDATE') ORDER BY OCCURRED_AT DESC LIMIT ?",this::mapEvent,tenantId,ns,st,sid,Math.min(Math.max(limit,1),200)); }
    @Override public List<MemoryEvent> findByNamespace(long tenantId,String ns,int limit) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE TENANT_ID=? AND NAMESPACE=? AND STATUS<>'REVOKED' ORDER BY OCCURRED_AT LIMIT ?",this::mapEvent,tenantId,ns,Math.min(Math.max(limit,1),100000)); }
    @Override public List<MemoryEvent> findByNamespace(String ns,int limit) { return jdbc.query("SELECT * FROM MEMORY_EVENT WHERE NAMESPACE=? AND STATUS<>'REVOKED' ORDER BY OCCURRED_AT LIMIT ?",this::mapEvent,ns,Math.min(Math.max(limit,1),100000)); }
    public void updateEventStatus(long tenantId,String id,MemoryEventStatus status) { jdbc.update("UPDATE MEMORY_EVENT SET STATUS=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE TENANT_ID=? AND ID=?",status.name(),tenantId,id); }
    @Override public void deactivateEdgesForNode(long tenantId, String nodeId) {
        jdbc.update("UPDATE MEMORY_EDGE SET ACTIVE=FALSE WHERE TENANT_ID=? AND (SOURCE_NODE_ID=? OR TARGET_NODE_ID=?)", tenantId, nodeId, nodeId);
    }
    public void upsertEntity(MemoryEntity e) { jdbc.update("MERGE INTO MEMORY_ENTITY (ID,TENANT_ID,ENTITY_TYPE,EXTERNAL_REF,DISPLAY_NAME,NORMALIZED_NAME,ATTRIBUTES,ACTIVE) KEY(TENANT_ID,ENTITY_TYPE,EXTERNAL_REF) VALUES (?,?,?,?,?,?,?,?)",e.id(),e.tenantId(),e.entityType(),e.externalRef(),e.displayName(),e.normalizedName(),JSONUtils.toJsonString(e.attributes()),e.active()); }
    public void insertEdge(MemoryEdge e) { jdbc.update("INSERT INTO MEMORY_EDGE (ID,TENANT_ID,SOURCE_NODE_ID,TARGET_NODE_ID,GRAPH_TYPE,RELATION_TYPE,DIRECTED,WEIGHT,CONFIDENCE,ORIGIN,EVIDENCE_SOURCE,ACTIVE,CREATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",e.id(),e.tenantId(),e.sourceNodeId(),e.targetNodeId(),e.graphType().name(),e.relationType(),e.directed(),e.weight(),e.confidence(),e.origin().name(),e.evidenceSource(),e.active(),ts(e.createdAt())); }
    public List<MemoryEdge> findEdges(long tenantId,String nodeId,GraphType graphType,int limit) { return jdbc.query("SELECT * FROM MEMORY_EDGE WHERE TENANT_ID=? AND (SOURCE_NODE_ID=? OR TARGET_NODE_ID=?) AND GRAPH_TYPE=? AND ACTIVE=TRUE ORDER BY CONFIDENCE DESC LIMIT ?",this::mapEdge,tenantId,nodeId,nodeId,graphType.name(),Math.min(Math.max(limit,1),200)); }
    public void enqueueConsolidation(long tenantId,String eventId) { Instant now=Instant.now(); jdbc.update("INSERT INTO MEMORY_CONSOLIDATION_JOB (TENANT_ID,EVENT_ID,STATUS,ATTEMPTS,AVAILABLE_AT,CREATED_AT,UPDATED_AT) VALUES (?,?, 'PENDING',0,?,?,?)",tenantId,eventId,ts(now),ts(now),ts(now)); }
    @Override public void recordRetrievalTrace(MemoryRetrievalTrace trace) { jdbc.update("INSERT INTO MEMORY_RETRIEVAL_TRACE (ID,TENANT_ID,NAMESPACE,QUERY_TEXT,ANCHOR_EVENT_IDS,CREATED_AT) VALUES (?,?,?,?,?,?)", trace.id(), trace.tenantId(), trace.namespace(), trace.queryText(), JSONUtils.toJsonString(trace.anchorEventIds()), ts(trace.createdAt())); }
    @Override public Optional<MemoryRetrievalTrace> findRetrievalTrace(long tenantId,String id) { return jdbc.query("SELECT * FROM MEMORY_RETRIEVAL_TRACE WHERE TENANT_ID=? AND ID=?", (r,n)->new MemoryRetrievalTrace(r.getString("ID"),r.getLong("TENANT_ID"),r.getString("NAMESPACE"),r.getString("QUERY_TEXT"),JSONUtils.parseObject(r.getString("ANCHOR_EVENT_IDS"),List.class),r.getTimestamp("CREATED_AT").toInstant()), tenantId,id).stream().findFirst(); }

    private MemoryEvent mapEvent(ResultSet r,int n)throws java.sql.SQLException { Map<String,Object> attrs=JSONUtils.parseObject(r.getString("ATTRIBUTES"),Map.class); return new MemoryEvent(r.getString("ID"),r.getLong("TENANT_ID"),r.getString("NAMESPACE"),r.getString("SUBJECT_TYPE"),r.getString("SUBJECT_ID"),r.getString("EVENT_TYPE"),r.getString("CONTENT"),r.getTimestamp("OCCURRED_AT").toInstant(),r.getString("SOURCE_TYPE"),r.getString("SOURCE_ID"),attrs,r.getDouble("CONFIDENCE"),r.getDouble("IMPORTANCE"),MemoryEventStatus.valueOf(r.getString("STATUS")),ConfirmationPolicy.valueOf(r.getString("CONFIRMATION_POLICY")),r.getTimestamp("CREATED_AT").toInstant(),r.getTimestamp("UPDATED_AT").toInstant()); }
    private MemoryEdge mapEdge(ResultSet r,int n)throws java.sql.SQLException { return new MemoryEdge(r.getString("ID"),r.getLong("TENANT_ID"),r.getString("SOURCE_NODE_ID"),r.getString("TARGET_NODE_ID"),GraphType.valueOf(r.getString("GRAPH_TYPE")),r.getString("RELATION_TYPE"),r.getBoolean("DIRECTED"),r.getDouble("WEIGHT"),r.getDouble("CONFIDENCE"),EdgeOrigin.valueOf(r.getString("ORIGIN")),r.getString("EVIDENCE_SOURCE"),r.getBoolean("ACTIVE"),r.getTimestamp("CREATED_AT").toInstant()); }
    private static Timestamp ts(Instant i){return Timestamp.from(i==null?Instant.now():i);}
}
