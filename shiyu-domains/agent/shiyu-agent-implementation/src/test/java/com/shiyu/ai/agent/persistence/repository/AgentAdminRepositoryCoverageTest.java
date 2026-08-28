package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.persistence.dataobject.AgentDefDO;
import com.shiyu.ai.agent.persistence.dataobject.AgentVersionDO;
import com.shiyu.ai.agent.persistence.mapper.AgentDefMapper;
import com.shiyu.ai.agent.persistence.mapper.AgentVersionMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentAdminRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(11);

    @Test
    void queriesAgentsAndVersionsWithTenantFilters() {
        AgentDefMapper defs = mock(AgentDefMapper.class); AgentVersionMapper versions = mock(AgentVersionMapper.class);
        AgentAdminRepositoryImpl repository = repository(defs, versions);
        AgentDefDO def = def(1L); AgentVersionDO version = version(2L);
        when(defs.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        when(defs.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(def));
        when(defs.selectOneByQuery(any(QueryWrapper.class))).thenReturn(def);
        when(versions.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(version));
        when(versions.selectOneByQuery(any(QueryWrapper.class))).thenReturn(version);
        AgentDefBO defBo = new AgentDefBO(); defBo.setId(1L); defBo.setAgentId("agent");
        AgentVersionBO versionBo = new AgentVersionBO(); versionBo.setId(2L); versionBo.setAgentId("agent");
        try (var conversion = mockStatic(MapstructUtils.class)) {
            conversion.when(() -> MapstructUtils.convert(anyList(), eq(AgentDefBO.class))).thenReturn(List.of(defBo));
            conversion.when(() -> MapstructUtils.convert(anyList(), eq(AgentVersionBO.class))).thenReturn(List.of(versionBo));
            conversion.when(() -> MapstructUtils.convert(any(AgentDefDO.class), eq(AgentDefBO.class))).thenReturn(defBo);
            conversion.when(() -> MapstructUtils.convert(any(AgentVersionDO.class), eq(AgentVersionBO.class))).thenReturn(versionBo);
            assertEquals(1L, repository.selectPage(TENANT, 1, 10, "demo", 1).getLeft());
            assertEquals(1L, repository.selectById(TENANT, 1L).getId());
            assertEquals(1L, repository.selectByAgentId(TENANT, "agent").getId());
            assertEquals(1, repository.selectAllActive(TENANT).size());
            assertEquals(1, repository.selectVersionsByAgentId(TENANT, "agent").size());
            assertEquals(2L, repository.selectVersionById(TENANT, 2L).getId());
            assertEquals(2L, repository.selectVersionByAgentIdAndNumber(TENANT, "agent", "v1").getId());
            repository.selectPage(TENANT, null, null, null, null);
        }
    }

    @Test
    void createsUpdatesDeletesAndRejectsCrossTenantRows() {
        AgentDefMapper defs = mock(AgentDefMapper.class); AgentVersionMapper versions = mock(AgentVersionMapper.class);
        AgentAdminRepositoryImpl repository = repository(defs, versions);
        doAnswer(invocation -> { AgentDefDO row = invocation.getArgument(0); row.setId(10L); return 1; }).when(defs).insertSelective(any(AgentDefDO.class));
        doAnswer(invocation -> { AgentVersionDO row = invocation.getArgument(0); row.setId(20L); return 1; }).when(versions).insertSelective(any(AgentVersionDO.class));
        when(defs.update(any(AgentDefDO.class))).thenReturn(1);
        when(versions.update(any(AgentVersionDO.class))).thenReturn(1);
        AgentDefBO def = new AgentDefBO(); def.setAgentId("agent"); def.setName("Demo");
        AgentDefDO mappedDef = def(10L); AgentVersionDO mappedVersion = version(20L);
        try (var conversion = mockStatic(MapstructUtils.class)) {
            conversion.when(() -> MapstructUtils.convert(any(AgentDefBO.class), eq(AgentDefDO.class))).thenReturn(mappedDef);
            conversion.when(() -> MapstructUtils.convert(any(AgentVersionBO.class), eq(AgentVersionDO.class))).thenReturn(mappedVersion);
            AgentDefBO created = repository.create(TENANT, def); assertEquals(10L, created.getId()); assertEquals(11L, created.getTenantId());
            when(defs.selectOneByQuery(any(QueryWrapper.class))).thenReturn(def(10L));
            created.setId(10L); assertSame(created, repository.update(TENANT, created)); repository.deleteById(TENANT, 10L); repository.deleteByAgentId(TENANT, "agent");
            AgentVersionBO vb = new AgentVersionBO(); vb.setAgentId("agent"); vb.setVersionNumber("v1");
            assertEquals(20L, repository.createVersion(TENANT, vb).getId());
            when(versions.selectOneByQuery(any(QueryWrapper.class))).thenReturn(version(20L));
            vb.setId(20L); assertSame(vb, repository.updateVersion(TENANT, vb)); repository.deleteVersionById(TENANT, 20L);
            when(defs.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            assertThrows(IllegalStateException.class, () -> repository.update(TENANT, created));
            when(versions.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            assertThrows(IllegalStateException.class, () -> repository.updateVersion(TENANT, vb));
        }
    }

    @Test
    void mapsMissingRowsToNull() {
        AgentDefMapper defs = mock(AgentDefMapper.class); AgentVersionMapper versions = mock(AgentVersionMapper.class);
        AgentAdminRepositoryImpl repository = repository(defs, versions);
        when(defs.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        when(versions.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertNull(repository.selectById(TENANT, 1L)); assertNull(repository.selectByAgentId(TENANT, "none"));
        assertNull(repository.selectVersionById(TENANT, 1L)); assertNull(repository.selectVersionByAgentIdAndNumber(TENANT, "none", "v1"));
        repository.deleteById(TENANT, 1L); repository.deleteByAgentId(TENANT, "none"); repository.deleteVersionById(TENANT, 1L);
    }

    @Test
    void rejectsAgentUpdateWhenTenantScopedWriteAffectsNoRows() {
        AgentDefMapper defs = mock(AgentDefMapper.class);
        AgentVersionMapper versions = mock(AgentVersionMapper.class);
        AgentAdminRepositoryImpl repository = repository(defs, versions);
        AgentDefBO value = new AgentDefBO();
        value.setId(10L);
        value.setTenantId(11L);
        when(defs.selectOneByQuery(any(QueryWrapper.class))).thenReturn(def(10L));
        when(defs.update(any(AgentDefDO.class))).thenReturn(0);
        try (var conversion = mockStatic(MapstructUtils.class)) {
            conversion.when(() -> MapstructUtils.convert(any(AgentDefBO.class), eq(AgentDefDO.class)))
                    .thenReturn(def(10L));
            assertThrows(IllegalStateException.class, () -> repository.update(TENANT, value));
        }
    }

    private static AgentAdminRepositoryImpl repository(AgentDefMapper defs, AgentVersionMapper versions) {
        AgentAdminRepositoryImpl repository = new AgentAdminRepositoryImpl();
        try {
            var f = AgentAdminRepositoryImpl.class.getDeclaredField("agentDefMapper"); f.setAccessible(true); f.set(repository, defs);
            f = AgentAdminRepositoryImpl.class.getDeclaredField("agentVersionMapper"); f.setAccessible(true); f.set(repository, versions);
            return repository;
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private static AgentDefDO def(long id) { AgentDefDO row = new AgentDefDO(); row.setId(id); row.setTenantId(11L); row.setAgentId("agent"); row.setName("Demo"); row.setStatus(1); row.setDelFlag(0); return row; }
    private static AgentVersionDO version(long id) { AgentVersionDO row = new AgentVersionDO(); row.setId(id); row.setTenantId(11L); row.setAgentId("agent"); row.setVersionNumber("v1"); row.setDelFlag(0); return row; }
}
