package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.port.repository.ExecutionTimelineRepository;
import com.shiyu.ai.common.foundation.utils.JSONUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提供 时间线 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class TimelineService {

    /**
     * timelineRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ExecutionTimelineRepository timelineRepository;

    /**
     * 执行 时间线 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param timelineRepository 用于完成本次业务处理的 timelineRepository 参数。
     */
    public TimelineService(ExecutionTimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    /** 写入节点执行开始事件 */
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
            log.warn(
                    "写入执行时间线失败 (NODE_START): executionIdPresent={}, nodeIdPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    event.getExecutionId() != null,
                    event.getNodeId() != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /** 写入节点执行完成事件 */
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
            log.warn(
                    "写入执行时间线失败 (NODE_END): executionIdPresent={}, nodeIdPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    event.getExecutionId() != null,
                    event.getNodeId() != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /** 查询执行时间线 */
    public List<Map<String, Object>> getTimeline(
            com.shiyu.ai.kernel.context.TenantId tenantId, String executionId) {
        return timelineRepository.listByExecutionId(tenantId, executionId).stream()
                .map(
                        doObj -> {
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
                        })
                .collect(Collectors.toList());
    }
}
