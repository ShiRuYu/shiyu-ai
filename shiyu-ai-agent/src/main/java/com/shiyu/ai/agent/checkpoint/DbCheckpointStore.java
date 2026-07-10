package com.shiyu.ai.agent.checkpoint;

import com.shiyu.ai.agent.checkpoint.Checkpoint;
import com.shiyu.ai.agent.checkpoint.CheckpointStore;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 基于数据库的检查点存储
 */
@Slf4j
public class DbCheckpointStore implements CheckpointStore {

    private final JdbcTemplate jdbcTemplate;

    public DbCheckpointStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Checkpoint checkpoint) {
        String stateJson = JSONUtils.toJsonString(checkpoint.getState());
        jdbcTemplate.update(
            "INSERT INTO agent_checkpoint (checkpoint_id, execution_id, node_id, state_data, create_time) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
            checkpoint.getCheckpointId(),
            checkpoint.getExecutionId(),
            checkpoint.getNodeId(),
            stateJson
        );
    }

    @Override
    public Checkpoint load(String checkpointId) {
        List<Checkpoint> results = jdbcTemplate.query(
            "SELECT * FROM agent_checkpoint WHERE checkpoint_id = ?",
            new CheckpointRowMapper(),
            checkpointId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Checkpoint loadByExecutionId(String executionId) {
        List<Checkpoint> results = jdbcTemplate.query(
            "SELECT * FROM agent_checkpoint WHERE execution_id = ? ORDER BY create_time DESC LIMIT 1",
            new CheckpointRowMapper(),
            executionId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void delete(String checkpointId) {
        jdbcTemplate.update("DELETE FROM agent_checkpoint WHERE checkpoint_id = ?", checkpointId);
    }

    @Override
    public void deleteByExecutionId(String executionId) {
        jdbcTemplate.update("DELETE FROM agent_checkpoint WHERE execution_id = ?", executionId);
    }

    @Override
    public List<Checkpoint> listByExecutionId(String executionId) {
        return jdbcTemplate.query(
            "SELECT * FROM agent_checkpoint WHERE execution_id = ? ORDER BY create_time ASC",
            new CheckpointRowMapper(),
            executionId
        );
    }

    private static class CheckpointRowMapper implements RowMapper<Checkpoint> {
        @Override
        public Checkpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
            Checkpoint cp = new Checkpoint(
                rs.getString("execution_id"),
                rs.getString("node_id"),
                JSONUtils.parseObject(rs.getString("state_data"), Map.class)
            );
            return cp;
        }
    }
}
