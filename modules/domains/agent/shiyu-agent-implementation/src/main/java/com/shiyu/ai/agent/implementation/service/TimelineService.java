package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.event.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.NodeExecutionStartedEvent;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.agent.implementation.port.repository.ExecutionTimelineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 执行时间线服务
 * <p>
 * 记录 Agent 执行过程中每个节点的开始/结束事件，
 * 提供按 executionId 查询完整时间线的能力。
 */
@Slf4j
@Service
public class TimelineService {

    private final ExecutionTimelineRepository timelineRepository;

    public TimelineService(ExecutionTimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    /**
     * 写入节点执行开始事件
     */
    public void onNodeStarted(NodeExecutionStartedEvent event) {
        try {
            ExecutionTimelineBO record = new ExecutionTimelineBO();
            record.setExecutionId(event.getExecutionId());
            record.setAgentId(event.getAgentId());
            record.setNodeId(event.getNodeId());
            record.setNodeType(event.getNodeType());
            record.setEventType("NODE_START");
            record.setPayload(JSONUtils.toJsonString(event.getInput()));
            record.setTenantId(event.getTenantId().value());
            record.setCreateTime(LocalDateTime.now());
            timelineRepository.insert(event.getTenantId(), record);
        } catch (Exception e) {
            log.warn("写入执行时间线失败 (NODE_START): executionIdPresent={}, nodeIdPresent={}, errorType={}, errorMessageLength={}",
                    event.getExecutionId() != null, event.getNodeId() != null,
                    e.getClass().getSimpleName(), e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /**
     * 写入节点执行完成事件
     */
    public void onNodeCompleted(NodeExecutionCompletedEvent event) {
        try {
            ExecutionTimelineBO record = new ExecutionTimelineBO();
            record.setExecutionId(event.getExecutionId());
            record.setAgentId(event.getAgentId());
            record.setNodeId(event.getNodeId());
            record.setNodeType(event.getNodeType());
            record.setEventType("NODE_END");
            record.setPayload(JSONUtils.toJsonString(event.getOutput()));
            record.setDurationMs(event.getDurationMs());
            record.setTenantId(event.getTenantId().value());
            record.setCreateTime(LocalDateTime.now());
            timelineRepository.insert(event.getTenantId(), record);
        } catch (Exception e) {
            log.warn("写入执行时间线失败 (NODE_END): executionIdPresent={}, nodeIdPresent={}, errorType={}, errorMessageLength={}",
                    event.getExecutionId() != null, event.getNodeId() != null,
                    e.getClass().getSimpleName(), e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /**
     * 查询执行时间线
     */
    public List<Map<String, Object>> getTimeline(com.shiyu.ai.kernel.context.TenantId tenantId, String executionId) {
        return timelineRepository.listByExecutionId(tenantId, executionId)
                .stream().map(doObj -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", doObj.getId());
                    map.put("executionId", doObj.getExecutionId());
                    map.put("agentId", doObj.getAgentId());
                    map.put("nodeId", doObj.getNodeId());
                    map.put("nodeType", doObj.getNodeType());
                    map.put("eventType", doObj.getEventType());
                    map.put("payload", doObj.getPayload());
                    map.put("durationMs", doObj.getDurationMs());
                    map.put("createTime", doObj.getCreateTime());
                    return map;
                }).collect(Collectors.toList());
    }
}
