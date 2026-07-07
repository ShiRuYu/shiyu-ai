package com.shiyu.ai.aiagent.service.impl;

import com.shiyu.ai.dal.repository.agent.AgentAdminRepository;
import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.aiagent.service.AgentVersionService;
import com.shiyu.ai.aiagent.bo.AgentDefBO;
import com.shiyu.ai.aiagent.bo.AgentVersionBO;
import com.shiyu.ai.aiagent.request.VersionRequest;
import com.shiyu.ai.aiagent.vo.AgentVersionDetailVO;
import com.shiyu.ai.aiagent.vo.AgentVersionVO;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentVersionServiceImpl implements AgentVersionService {

    @Resource
    private AgentAdminRepository agentAdminRepository;

    @Resource
    private AgentService agentService;

    @Override
    public List<AgentVersionVO> getVersions(String agentId) {
        List<AgentVersionBO> versions = agentAdminRepository.selectVersionsByAgentId(agentId);
        return versions.stream().map(this::toVersionVO).collect(Collectors.toList());
    }

    @Override
    public AgentVersionDetailVO getVersionDetail(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return toVersionDetailVO(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVersionVO createVersion(String agentId, VersionRequest request) {
        AgentDefBO def = agentAdminRepository.selectByAgentId(agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在: " + agentId);

        AgentVersionBO existing = agentAdminRepository.selectVersionByAgentIdAndNumber(agentId, request.getVersionNumber());
        if (existing != null) throw new IllegalArgumentException("版本号已存在: " + request.getVersionNumber());

        AgentVersionBO version = new AgentVersionBO();
        version.setAgentId(agentId);
        version.setVersionNumber(request.getVersionNumber());
        version.setDescription(request.getDescription());
        version.setStatus("DRAFT");

        if (request.getCopyFromVersionId() != null) {
            AgentVersionBO source = agentAdminRepository.selectVersionById(request.getCopyFromVersionId());
            if (source != null) {
                version.setGraphConfig(source.getGraphConfig());
                version.setCanvasConfig(source.getCanvasConfig());
            }
        }

        agentAdminRepository.createVersion(version);
        evictAgentCache(agentId);
        return toVersionVO(version);
    }

    @Override
    public AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在: " + versionId);
        if (request.getDescription() != null) v.setDescription(request.getDescription());
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        return toVersionVO(v);
    }

    @Override
    public void deleteVersion(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return;
        agentAdminRepository.deleteVersionById(versionId);
        evictAgentCache(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if (!"DRAFT".equals(v.getStatus())) throw new IllegalArgumentException("只有草稿状态才能发布");
        v.setStatus("PUBLISHED");
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        evictAgentCache(agentId);
    }

    @Override
    public void archiveVersion(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if ("ARCHIVED".equals(v.getStatus())) throw new IllegalArgumentException("版本已归档");
        v.setStatus("ARCHIVED");
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        evictAgentCache(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateVersion(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if (!"PUBLISHED".equals(v.getStatus())) throw new IllegalArgumentException("只有已发布版本才能激活");

        AgentDefBO def = agentAdminRepository.selectByAgentId(agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在");
        def.setCurrentVersion(v.getVersionNumber());
        def.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.update(def);

        evictAgentCache(agentId);
    }

    @Override
    public AgentVersionVO copyVersion(String agentId, VersionRequest request) {
        return createVersion(agentId, request);
    }

    // ========== Private helpers ==========

    private void evictAgentCache(String agentId) {
        agentService.evictRuntimeCache(agentId);
    }

    private AgentVersionVO toVersionVO(AgentVersionBO v) {
        return AgentVersionVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    private AgentVersionDetailVO toVersionDetailVO(AgentVersionBO v) {
        AgentVersionDetailVO.GraphConfigVO graphVO = null;
        if (v.getGraphConfig() != null && !v.getGraphConfig().isEmpty()) {
            try {
                Map<String, Object> graphData = JSONUtils.parseObject(v.getGraphConfig(),
                        new tools.jackson.core.type.TypeReference<Map<String, Object>>(){});
                graphVO = AgentVersionDetailVO.GraphConfigVO.builder()
                        .name((String) graphData.get("name"))
                        .description((String) graphData.get("description"))
                        .startNode((String) graphData.get("startNode"))
                        .endNode((String) graphData.get("endNode"))
                        .nodes(getMap(graphData, "nodes"))
                        .edges(getMap(graphData, "edges"))
                        .conditionalEdges(getMap(graphData, "conditionalEdges"))
                        .build();
            } catch (Exception ignored) {
            }
        }
        return AgentVersionDetailVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .graphConfig(graphVO).canvasConfig(v.getCanvasConfig())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        Object value = parent.computeIfAbsent(key, k -> new java.util.LinkedHashMap<>());
        return (Map<String, Object>) value;
    }
}
