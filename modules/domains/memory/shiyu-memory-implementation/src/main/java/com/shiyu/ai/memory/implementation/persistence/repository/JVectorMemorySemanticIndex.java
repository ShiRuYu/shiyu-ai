package com.shiyu.ai.memory.implementation.persistence.repository;

import com.shiyu.ai.memory.magma.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorSearchRequest;
import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JVectorMemorySemanticIndex implements MemorySemanticIndex {
    private final VectorStore store;
    private final VectorStoreProperties properties;
    private final JdbcMagmaMemoryRepository repository;
    public JVectorMemorySemanticIndex(VectorStore store, VectorStoreProperties properties, JdbcMagmaMemoryRepository repository){this.store=store;this.properties=properties;this.repository=repository;}
    public void upsert(MemoryEvent event){Map<String,Object> metadata=new HashMap<>();metadata.put("tenantId",event.tenantId());metadata.put("namespace",event.namespace());metadata.put("subjectType",event.subjectType());metadata.put("subjectId",event.subjectId());metadata.put("status",event.status().name());store.upsert(new VectorRecord(event.id(),embed(event.content()),metadata));store.flush();}
    public List<MemoryPath> search(MemoryQuery query,int limit){Map<String,Object> filter=new HashMap<>();filter.put("tenantId",query.tenantId().value());filter.put("namespace",query.namespace());if(query.subjectType()!=null&&!query.subjectType().isBlank())filter.put("subjectType",query.subjectType());if(query.subjectId()!=null&&!query.subjectId().isBlank())filter.put("subjectId",query.subjectId());List<VectorRecord> records=store.search(VectorSearchRequest.builder().queryVector(embed(query.text())).topK(limit).filter(filter).build());List<MemoryPath> out=new ArrayList<>();for(VectorRecord r:records){repository.findEvent(query.tenantId(),r.id()).filter(e->e.status()==MemoryEventStatus.ACTIVE).ifPresent(e->out.add(new MemoryPath(e,((Number)r.metadata().getOrDefault("_score",0d)).doubleValue(),List.of())));}return out;}
    public void delete(String id){store.delete(id);store.flush();}
    public void rebuild(TenantId tenantId, String namespace){
        // Namespace rebuild is additive and never clears unrelated tenants/namespaces.
        // A full store rebuild remains available to the vector SPI for disaster recovery.
        repository.findByNamespace(tenantId,namespace,100000).forEach(this::upsert);
    }
    private float[] embed(String text){int d=Math.max(8,properties.getDimension());float[] v=new float[d];byte[] bytes=text.getBytes(StandardCharsets.UTF_8);for(int i=0;i<bytes.length;i++)v[i%d]+=((bytes[i]&0xff)-128)/128f;float norm=0;for(float x:v)norm+=x*x;norm=(float)Math.sqrt(norm);if(norm>0)for(int i=0;i<v.length;i++)v[i]/=norm;return v;}
}

