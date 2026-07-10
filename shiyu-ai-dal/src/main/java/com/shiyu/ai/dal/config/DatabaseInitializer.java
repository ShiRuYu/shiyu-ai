package com.shiyu.ai.dal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
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
 *
 * <p>自动扫描 classpath:db/migration/ddl/*.sql 和 classpath:db/migration/data/*.sql，
 * 按文件名排序后依次执行，无需手动维护列表。</p>
 */
@Slf4j
@Component
@Order(0)
public class DatabaseInitializer implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public DatabaseInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.resourceResolver = new PathMatchingResourcePatternResolver(applicationContext);
    }

    @Override
    public void run(ApplicationArguments args) {
        // 如果 application context 中有 flyway bean，跳过手动 DDL 执行
        if (applicationContext.containsBean("flyway")) {
            log.info("Flyway 已启用，跳过 DatabaseInitializer DDL 执行");
            return;
        }
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
            for (Resource res : listSqlResources("classpath:db/migration/ddl/*.sql")) {
                executeSqlFile(stmt, res);
            }
            // 2. 执行 DML（数据）
            log.info("开始执行 DML 种子数据...");
            for (Resource res : listSqlResources("classpath:db/migration/data/*.sql")) {
                executeSqlFile(stmt, res);
            }
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    /**
     * 扫描指定路径的所有 SQL 文件，按文件名排序
     */
    private Resource[] listSqlResources(String pattern) throws Exception {
        Resource[] resources = resourceResolver.getResources(pattern);
        Arrays.sort(resources, (a, b) -> {
            try {
                return a.getFilename().compareTo(b.getFilename());
            } catch (Exception e) {
                return 0;
            }
        });
        return resources;
    }

    private void executeSqlFile(Statement stmt, Resource resource) {
        try {
            if (!resource.exists()) {
                log.warn("SQL 文件不存在: {}", resource.getFilename());
                return;
            }
            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                sql = sb.toString();
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
            log.info("SQL 文件执行完成: {}", resource.getFilename());
        } catch (Exception e) {
            log.error("SQL 文件执行失败: {}", resource.getFilename(), e);
        }
    }
}
