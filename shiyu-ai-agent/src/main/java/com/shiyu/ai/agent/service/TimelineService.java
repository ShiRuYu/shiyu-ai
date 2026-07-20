package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.event.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.event.NodeExecutionStartedEvent;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.dal.agent.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.dal.agent.repository.ExecutionTimelineRepository;
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
            ExecutionTimelineDO record = new ExecutionTimelineDO();
            record.setExecutionId(event.getExecutionId());
            record.setAgentId(event.getAgentId());
            record.setNodeId(event.getNodeId());
            record.setNodeType(event.getNodeType());
            record.setEventType("NODE_START");
            record.setPayload(JSONUtils.toJsonString(event.getInput()));
            record.setCreateTime(LocalDateTime.now());
            timelineRepository.insert(record);
        } catch (Exception e) {
            log.warn("写入执行时间线失败 (NODE_START): executionId={}, nodeId={}",
                    event.getExecutionId(), event.getNodeId(), e);
        }
    }

    /**
     * 写入节点执行完成事件
     */
    public void onNodeCompleted(NodeExecutionCompletedEvent event) {
        try {
            ExecutionTimelineDO record = new ExecutionTimelineDO();
            record.setExecutionId(event.getExecutionId());
            record.setAgentId(event.getAgentId());
            record.setNodeId(event.getNodeId());
            record.setNodeType(event.getNodeType());
            record.setEventType("NODE_END");
            record.setPayload(JSONUtils.toJsonString(event.getOutput()));
            record.setDurationMs(event.getDurationMs());
            record.setCreateTime(LocalDateTime.now());
            timelineRepository.insert(record);
        } catch (Exception e) {
            log.warn("写入执行时间线失败 (NODE_END): executionId={}, nodeId={}",
                    event.getExecutionId(), event.getNodeId(), e);
        }
    }

    /**
     * 查询执行时间线
     */
    public List<Map<String, Object>> getTimeline(String executionId) {
        return timelineRepository.listByExecutionId(executionId)
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
