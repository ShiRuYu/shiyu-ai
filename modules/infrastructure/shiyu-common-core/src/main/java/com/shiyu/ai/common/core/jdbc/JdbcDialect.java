package com.shiyu.ai.common.core.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * 提供数据库方言和 SQL 能力判断。
 */
public final class JdbcDialect {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    /**
     * kind 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Kind kind;

    private JdbcDialect(Kind kind) {
        this.kind = kind;
    }

    /**
     * {@code detect} 执行当前类型定义的业务操作。
     *
     * @param jdbc 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static JdbcDialect detect(JdbcTemplate jdbc) {
        Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        DataSource dataSource = jdbc.getDataSource();
        if (dataSource == null) return new JdbcDialect(Kind.H2);
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return fromProduct(product);
        } catch (SQLException | RuntimeException ignored) {
            return new JdbcDialect(Kind.H2);
        }
    }

    /**
     * {@code fromProduct} 执行当前类型定义的业务操作。
     *
     * @param product 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static JdbcDialect fromProduct(String product) {
        String normalized = product == null ? "" : product.toLowerCase(Locale.ROOT);
        if (normalized.contains("postgres")) return new JdbcDialect(Kind.POSTGRESQL);
        if (normalized.contains("mysql")) return new JdbcDialect(Kind.MYSQL);
        if (normalized.contains("h2")) return new JdbcDialect(Kind.H2);
        return new JdbcDialect(Kind.UNKNOWN);
    }

    /**
     * {@code kind} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Kind kind() {
        return kind;
    }

    /**
     * 处理upsert。
     *
     * @param table table 参数。
     * @param columns columns 参数。
     * @param valuesSql valuesSql 参数。
     * @param conflictColumns conflictColumns 参数。
     * @param updateColumns updateColumns 参数。
     *
     * @return 结果列表。
     */
    public String upsert(
            String table,
            List<String> columns,
            String valuesSql,
            List<String> conflictColumns,
            List<String> updateColumns) {
        identifier(table);
        identifiers(columns);
        identifiers(conflictColumns);
        identifiers(updateColumns);
        Objects.requireNonNull(valuesSql, "valuesSql must not be null");
        String columnSql = String.join(", ", columns);
        String conflictSql = String.join(", ", conflictColumns);
        if (kind == Kind.POSTGRESQL) {
            if (updateColumns.isEmpty()) {
                return "INSERT INTO "
                        + table
                        + " ("
                        + columnSql
                        + ") VALUES ("
                        + valuesSql
                        + ") ON CONFLICT ("
                        + conflictSql
                        + ") DO NOTHING";
            }
            String updates =
                    updateColumns.stream()
                            .map(column -> column + "=EXCLUDED." + column)
                            .reduce((left, right) -> left + ", " + right)
                            .orElseThrow();
            return "INSERT INTO "
                    + table
                    + " ("
                    + columnSql
                    + ") VALUES ("
                    + valuesSql
                    + ") ON CONFLICT ("
                    + conflictSql
                    + ") DO UPDATE SET "
                    + updates;
        }
        if (kind == Kind.MYSQL) {
            String updates =
                    updateColumns.stream()
                            .map(column -> column + "=VALUES(" + column + ")")
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("1=1");
            return "INSERT INTO "
                    + table
                    + " ("
                    + columnSql
                    + ") VALUES ("
                    + valuesSql
                    + ") ON DUPLICATE KEY UPDATE "
                    + updates;
        }
        return "MERGE INTO "
                + table
                + " ("
                + columnSql
                + ") KEY("
                + conflictSql
                + ") VALUES ("
                + valuesSql
                + ")";
    }

    /**
     * 处理retrytimestampexpression。
     *
     * @param attemptsColumn attemptsColumn 参数。
     *
     * @return 处理结果。
     */
    public String retryTimestampExpression(String attemptsColumn) {
        identifier(attemptsColumn);
        return switch (kind) {
            case POSTGRESQL ->
                    "CURRENT_TIMESTAMP + (POWER(2, " + attemptsColumn + ") * INTERVAL '1 second')";
            case MYSQL ->
                    "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL POWER(2, " + attemptsColumn + ") SECOND)";
            case H2, UNKNOWN ->
                    "DATEADD('SECOND', POWER(2, " + attemptsColumn + "), CURRENT_TIMESTAMP)";
        };
    }

    private static void identifiers(List<String> identifiers) {
        Objects.requireNonNull(identifiers, "identifiers must not be null");
        identifiers.forEach(JdbcDialect::identifier);
    }

    private static void identifier(String identifier) {
        if (identifier == null || !IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Unsafe SQL identifier: " + identifier);
        }
    }

    /**
     * {@code Kind} 表示平台基础设施模块中的一组受控业务状态或分类。
     */
    public enum Kind {
        H2,
        MYSQL,
        POSTGRESQL,
        UNKNOWN
    }
}
