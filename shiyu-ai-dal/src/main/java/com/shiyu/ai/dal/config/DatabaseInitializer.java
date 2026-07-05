package com.shiyu.ai.dal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库表结构及种子数据初始化器
 *
 * 按以下顺序执行 SQL 文件：
 * 1. DDL（建表）→ 2. DML（数据）
 * 每个子目录内按文件编号顺序执行。
 */
@Slf4j
@Component
@Order(0)
public class DatabaseInitializer implements ApplicationRunner {

    private final ApplicationContext applicationContext;

    /** DDL 建表文件路径（按依赖顺序） */
    private static final List<String> DDL_FILES = List.of(
            "db/migration/ddl/01__schema_common.sql",
            "db/migration/ddl/02__schema_auth.sql",
            "db/migration/ddl/03__schema_agent.sql",
            "db/migration/ddl/04__schema_memory.sql",
            "db/migration/ddl/05__schema_knowledge.sql",
            "db/migration/ddl/06__schema_education.sql",
            "db/migration/ddl/07__schema_record.sql",
            "db/migration/ddl/08__schema_vector.sql"
    );

    /** DML 种子数据文件路径（按依赖顺序） */
    private static final List<String> DML_FILES = List.of(
            "db/migration/data/10__data_auth.sql",
            "db/migration/data/11__data_common.sql",
            "db/migration/data/12__data_agent.sql",
            "db/migration/data/13__data_record.sql",
            "db/migration/data/14__data_knowledge.sql",
            "db/migration/data/15__data_education.sql",
            "db/migration/data/16__data_memory.sql",
            "db/migration/data/17__data_menu_student_biz.sql",
            "db/migration/data/18__data_menu_edu_center.sql"
    );

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
            // 1. 执行 DDL（建表）
            log.info("开始执行 DDL 建表...");
            for (String path : DDL_FILES) {
                executeSqlFile(stmt, path);
            }
            // 2. 执行 DML（数据）
            log.info("开始执行 DML 种子数据...");
            for (String path : DML_FILES) {
                executeSqlFile(stmt, path);
            }
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    private void executeSqlFile(Statement stmt, String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.warn("SQL 文件不存在: {}", path);
                return;
            }
            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
            // 移除注释
            sql = sql.replaceAll("--[^\r\n]*", "");
            // 按分号分割，但忽略字符串内的分号
            StringBuilder current = new StringBuilder();
            boolean inString = false;
            char stringChar = 0;
            for (int i = 0; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (!inString && (c == '\'' || c == '"')) {
                    inString = true;
                    stringChar = c;
                    current.append(c);
                } else if (inString && c == stringChar) {
                    inString = false;
                    current.append(c);
                } else if (!inString && c == ';') {
                    String trimmed = current.toString().trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
            // 执行最后一个语句（如果没有分号结尾）
            String trimmed = current.toString().trim();
            if (!trimmed.isEmpty()) {
                stmt.execute(trimmed);
            }
            log.info("SQL 文件执行完成: {}", path);
        } catch (Exception e) {
            log.error("SQL 文件执行失败: {}", path, e);
        }
    }
}
