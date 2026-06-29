package com.shiyu.ai.aiagent.config;

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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(0)
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
            executeSqlFile(stmt, "db/migration/agent__init.sql");
            executeSqlFile(stmt, "db/migration/memory__init.sql");
            executeSqlFile(stmt, "db/migration/auth__init.sql");
            executeSqlFile(stmt, "db/migration/common__init.sql");
            executeSqlFile(stmt, "db/migration/record__init.sql");
            executeSqlFile(stmt, "db/migration/knowledge__init.sql");

            log.info("数据库表初始化完成");
        } catch (Exception e) {
            log.error("数据库表初始化失败", e);
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
