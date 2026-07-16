package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.event.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.event.NodeExecutionStartedEvent;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 执行时间线服务
 * <p>
 * 记录 Agent 执行过程中每个节点的开始/结束事件，
 * 提供按 executionId 查询完整时间线的能力。
 */
@Slf4j
@Service
public class TimelineService {

    private final JdbcTemplate jdbcTemplate;

    public TimelineService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 写入节点执行开始事件
     */
    public void onNodeStarted(NodeExecutionStartedEvent event) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO execution_timeline (execution_id, agent_id, node_id, node_type, " +
                    "event_type, payload, create_time) VALUES (?, ?, ?, ?, 'NODE_START', ?, ?)",
                    event.getExecutionId(), event.getAgentId(), event.getNodeId(), event.getNodeType(),
                    JSONUtils.toJsonString(event.getInput()), LocalDateTime.now());
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
            jdbcTemplate.update(
                    "INSERT INTO execution_timeline (execution_id, agent_id, node_id, node_type, " +
                    "event_type, payload, duration_ms, create_time) VALUES (?, ?, ?, ?, 'NODE_END', ?, ?, ?)",
                    event.getExecutionId(), event.getAgentId(), event.getNodeId(), event.getNodeType(),
                    JSONUtils.toJsonString(event.getOutput()), event.getDurationMs(), LocalDateTime.now());
        } catch (Exception e) {
            log.warn("写入执行时间线失败 (NODE_END): executionId={}, nodeId={}",
                    event.getExecutionId(), event.getNodeId(), e);
        }
    }

    /**
     * 查询执行时间线
     *
     * @param executionId 执行 ID
     * @return 时间线条目列表
     */
    public List<Map<String, Object>> getTimeline(String executionId) {
        return jdbcTemplate.queryForList(
                "SELECT id, execution_id, agent_id, node_id, node_type, event_type, " +
                "payload, duration_ms, create_time FROM execution_timeline " +
                "WHERE execution_id = ? ORDER BY id ASC",
                executionId);
    }
}
