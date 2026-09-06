package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcAiAppRepository implements AiAppRepository {
    private final JdbcTemplate jdbc;
    public JdbcAiAppRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void insert(AiApp a) { jdbc.update("INSERT INTO AI_APP (ID,TENANT_ID,OWNER_USER_ID,NAME,DESCRIPTION,STATUS,PUBLISHED_VERSION_ID,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?)",a.id(),a.tenantId().value(),a.ownerUserId().value(),a.name(),a.description(),a.status(),a.publishedVersionId(),Timestamp.from(a.createdAt()),Timestamp.from(a.updatedAt())); }
    @Override public Optional<AiApp> find(String id,TenantId tenant,long owner) { return jdbc.query("SELECT * FROM AI_APP WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?",rs->rs.next()?Optional.of(mapApp(rs)):Optional.empty(),id,tenant(tenant),owner); }
    @Override public Optional<AiApp> findByTenant(String id,TenantId tenant) { return jdbc.query("SELECT * FROM AI_APP WHERE ID=? AND TENANT_ID=?",rs->rs.next()?Optional.of(mapApp(rs)):Optional.empty(),id,tenant(tenant)); }
    @Override public List<AiApp> list(TenantId tenant,long owner,int limit) { return jdbc.query("SELECT * FROM AI_APP WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT DESC LIMIT ?",(rs,n)->mapApp(rs),tenant(tenant),owner,Math.max(1,Math.min(limit,100))); }
    @Override public void insertVersion(AiAppVersion v) { jdbc.update("INSERT INTO AI_APP_VERSION (ID,APP_ID,TENANT_ID,VERSION,CONFIG_JSON,STATUS,CREATED_AT,PUBLISHED_AT) VALUES (?,?,?,?,?,?,?,?)",v.id(),v.appId(),v.tenantId().value(),v.version(),v.configJson(),v.status(),Timestamp.from(v.createdAt()),v.publishedAt()==null?null:Timestamp.from(v.publishedAt())); }
    @Override public Optional<AiAppVersion> findVersion(String appId,String id,TenantId tenant) { return jdbc.query("SELECT * FROM AI_APP_VERSION WHERE APP_ID=? AND ID=? AND TENANT_ID=?",rs->rs.next()?Optional.of(mapVersion(rs)):Optional.empty(),appId,id,tenant(tenant)); }
    @Override public List<AiAppVersion> versions(String appId,TenantId tenant) { return jdbc.query("SELECT * FROM AI_APP_VERSION WHERE APP_ID=? AND TENANT_ID=? ORDER BY CREATED_AT DESC",(rs,n)->mapVersion(rs),appId,tenant(tenant)); }
    @Override public int publishVersion(String appId,String id,TenantId tenant) { long tenantValue=tenant(tenant); int n=jdbc.update("UPDATE AI_APP_VERSION SET STATUS='PUBLISHED',PUBLISHED_AT=CURRENT_TIMESTAMP WHERE APP_ID=? AND ID=? AND TENANT_ID=? AND STATUS='DRAFT'",appId,id,tenantValue); if(n==1) jdbc.update("UPDATE AI_APP_VERSION SET STATUS='ARCHIVED' WHERE APP_ID=? AND TENANT_ID=? AND STATUS='PUBLISHED' AND ID<>?",appId,tenantValue,id); if(n==1) jdbc.update("UPDATE AI_APP SET PUBLISHED_VERSION_ID=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=? AND TENANT_ID=?",id,appId,tenantValue); return n; }
    @Override public int archiveVersion(String appId,String id,TenantId tenant) { return jdbc.update("UPDATE AI_APP_VERSION SET STATUS='ARCHIVED' WHERE APP_ID=? AND ID=? AND TENANT_ID=?",appId,id,tenant(tenant)); }
    private AiApp mapApp(java.sql.ResultSet rs)throws java.sql.SQLException{return new AiApp(rs.getString("ID"),new TenantId(rs.getLong("TENANT_ID")),new UserId(rs.getLong("OWNER_USER_ID")),rs.getString("NAME"),rs.getString("DESCRIPTION"),rs.getString("STATUS"),rs.getString("PUBLISHED_VERSION_ID"),rs.getTimestamp("CREATED_AT").toInstant(),rs.getTimestamp("UPDATED_AT").toInstant());}
    private AiAppVersion mapVersion(java.sql.ResultSet rs)throws java.sql.SQLException{return new AiAppVersion(rs.getString("ID"),rs.getString("APP_ID"),new TenantId(rs.getLong("TENANT_ID")),rs.getString("VERSION"),rs.getString("CONFIG_JSON"),rs.getString("STATUS"),rs.getTimestamp("CREATED_AT").toInstant(),rs.getTimestamp("PUBLISHED_AT")==null?null:rs.getTimestamp("PUBLISHED_AT").toInstant());}
    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}

