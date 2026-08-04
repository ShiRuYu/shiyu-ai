package com.shiyu.ai.dal.config;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

/**
 * 数据库表结构及种子数据初始化器
 */
@Slf4j
@Component
public class DatabaseInitializer {

    private final Map<String, DataSource> dataSources;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public DatabaseInitializer(Map<String, DataSource> dataSources, ApplicationContext applicationContext) {
        this.dataSources = dataSources;
        this.resourceResolver = new PathMatchingResourcePatternResolver(applicationContext);
    }

    @PostConstruct
    public void initialize() {
        DataSource ds = dataSources.get("agent");
        if (ds == null) ds = dataSources.get("agentDataSource");
        if (ds == null && !dataSources.isEmpty()) ds = dataSources.values().iterator().next();
        if (ds == null) { log.warn("未找到 DataSource，跳过数据库初始化"); return; }

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            log.info("开始执行 DDL 建表...");
            for (Resource res : listSqlResources("classpath:db/migration/**/ddl/*.sql")) {
                executeSqlFile(stmt, res);
            }
            log.info("开始执行 DML 种子数据...");
            for (Resource res : listSqlResources("classpath:db/migration/**/data/*.sql")) {
                executeSqlFile(stmt, res);
            }
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    private Resource[] listSqlResources(String pattern) throws Exception {
        Resource[] resources = resourceResolver.getResources(pattern);
        Arrays.sort(resources, (a, b) -> {
            try { return a.getFilename().compareTo(b.getFilename()); }
            catch (Exception e) { return 0; }
        });
        return resources;
    }

    private void executeSqlFile(Statement stmt, Resource resource) {
        try {
            if (!resource.exists()) { return; }
            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append('\n');
                sql = sb.toString();
            }
            sql = sql.replaceAll("--[^\r\n]*", "");
            StringBuilder current = new StringBuilder();
            boolean inString = false;
            char stringChar = 0;
            for (int i = 0; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (!inString && (c == '\'' || c == '"')) { inString = true; stringChar = c; current.append(c); }
                else if (inString && c == stringChar) { inString = false; current.append(c); }
                else if (!inString && c == ';') {
                    String trimmed = current.toString().trim();
                    if (!trimmed.isEmpty()) stmt.execute(trimmed);
                    current = new StringBuilder();
                } else { current.append(c); }
            }
            String trimmed = current.toString().trim();
            if (!trimmed.isEmpty()) stmt.execute(trimmed);
            log.info("SQL 文件执行完成: {}", resource.getFilename());
        } catch (Exception e) {
            log.error("SQL 文件执行失败: {}", resource.getFilename(), e);
        }
    }
}
