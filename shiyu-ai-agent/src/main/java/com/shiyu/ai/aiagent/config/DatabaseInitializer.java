package com.shiyu.ai.aiagent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

@Slf4j
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final ApplicationContext applicationContext;

    public DatabaseInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, DataSource> beans = applicationContext.getBeansOfType(DataSource.class);
        DataSource ds = beans.get("agent");
        if (ds == null) {
            ds = beans.get("agentDataSource");
        }
        if (ds == null && !beans.isEmpty()) {
            ds = beans.values().iterator().next();
        }
        if (ds == null) {
            log.warn("未找到 DataSource，跳过数据库表初始化");
            return;
        }
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS `conversation_message`");
            stmt.execute("""
                CREATE TABLE `conversation_message` (
                    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                    `session_id`  VARCHAR(64)  NOT NULL COMMENT '会话ID',
                    `user_id`     BIGINT       DEFAULT NULL,
                    `agent_id`    VARCHAR(64)  DEFAULT NULL,
                    `tenant_id`   BIGINT       NOT NULL COMMENT '租户ID',
                    `role`        VARCHAR(16)  NOT NULL COMMENT '角色(user/assistant/system/tool)',
                    `content`     TEXT         NOT NULL COMMENT '消息内容',
                    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
                    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
                    `update_time` TIMESTAMP    DEFAULT NULL COMMENT '更新时间',
                    PRIMARY KEY (`id`)
                )
            """);
            stmt.execute("CREATE INDEX `idx_conv_msg_session` ON `conversation_message` (`session_id`, `create_time`)");

            stmt.execute("DROP TABLE IF EXISTS `long_term_memory`");
            stmt.execute("""
                CREATE TABLE `long_term_memory` (
                    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                    `user_id`       BIGINT       DEFAULT NULL,
                    `agent_id`      VARCHAR(64)  DEFAULT NULL,
                    `tenant_id`     BIGINT       NOT NULL COMMENT '租户ID',
                    `category`      VARCHAR(64)  DEFAULT 'general' COMMENT '记忆分类',
                    `memory_key`    VARCHAR(255) NOT NULL COMMENT '记忆键',
                    `content`       TEXT         NOT NULL COMMENT '记忆内容',
                    `importance`    DOUBLE       DEFAULT 0.5 COMMENT '重要度评分(0-1)',
                    `source`        VARCHAR(64)  DEFAULT NULL COMMENT '来源(session_id等)',
                    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
                    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
                    `update_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`)
                )
            """);
            stmt.execute("CREATE INDEX `idx_ltm_user_agent` ON `long_term_memory` (`user_id`, `agent_id`)");
            stmt.execute("CREATE INDEX `idx_ltm_category` ON `long_term_memory` (`category`)");

            stmt.execute("DROP TABLE IF EXISTS `agent_execution`");
            stmt.execute("""
                CREATE TABLE `agent_execution` (
                    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                    `execution_id`  VARCHAR(64)  NOT NULL COMMENT '执行唯一ID(UUID)',
                    `agent_id`      VARCHAR(64)  NOT NULL COMMENT 'Agent标识',
                    `version`       VARCHAR(32)  DEFAULT NULL COMMENT '版本号',
                    `user_id`       BIGINT       DEFAULT NULL,
                    `session_id`    VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
                    `tenant_id`     BIGINT       NOT NULL COMMENT '租户ID',
                    `node_id`       VARCHAR(64)  DEFAULT NULL COMMENT '节点ID',
                    `node_type`     VARCHAR(32)  DEFAULT NULL COMMENT '节点类型',
                    `input_data`    TEXT         DEFAULT NULL COMMENT '输入数据(JSON)',
                    `output_data`   TEXT         DEFAULT NULL COMMENT '输出数据(JSON)',
                    `status`        VARCHAR(16)  NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED',
                    `error_message` TEXT         DEFAULT NULL COMMENT '错误信息',
                    `start_time`    TIMESTAMP    NOT NULL COMMENT '开始时间',
                    `end_time`      TIMESTAMP    DEFAULT NULL COMMENT '结束时间',
                    `duration_ms`   BIGINT       DEFAULT NULL COMMENT '耗时(毫秒)',
                    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
                    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
                    `update_time`   TIMESTAMP    DEFAULT NULL COMMENT '更新时间',
                    PRIMARY KEY (`id`)
                )
            """);
            stmt.execute("CREATE INDEX `idx_agent_exec_exec_id` ON `agent_execution` (`execution_id`)");
            stmt.execute("CREATE INDEX `idx_agent_exec_agent` ON `agent_execution` (`agent_id`, `create_time`)");
            stmt.execute("CREATE INDEX `idx_agent_exec_session` ON `agent_execution` (`session_id`)");

            log.info("数据库表初始化完成: conversation_message, long_term_memory, agent_execution");
        } catch (Exception e) {
            log.error("数据库表初始化失败", e);
        }
    }
}
