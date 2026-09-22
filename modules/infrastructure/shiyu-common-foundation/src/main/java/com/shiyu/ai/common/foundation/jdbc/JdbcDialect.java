package com.shiyu.ai.common.foundation.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * 实现 Jdbc Dialect 相关的业务处理、协作逻辑或基础设施能力。
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
     * 执行 Jdbc Dialect 相关业务数据，并返回处理结果。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     * @return 返回 Jdbc Dialect 相关操作生成的结果数据。
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
     * 执行 Jdbc Dialect 相关业务数据，并返回处理结果。
     *
     * @param product 用于完成本次业务处理的 product 参数。
     * @return 返回 Jdbc Dialect 相关操作生成的结果数据。
     */
    public static JdbcDialect fromProduct(String product) {
        String normalized = product == null ? "" : product.toLowerCase(Locale.ROOT);
        if (normalized.contains("postgres")) return new JdbcDialect(Kind.POSTGRESQL);
        if (normalized.contains("mysql")) return new JdbcDialect(Kind.MYSQL);
        if (normalized.contains("h2")) return new JdbcDialect(Kind.H2);
        return new JdbcDialect(Kind.UNKNOWN);
    }

    /**
     * 执行 Jdbc Dialect 相关业务数据，并返回处理结果。
     *
     * @return 返回 Jdbc Dialect 相关操作生成的结果数据。
     */
    public Kind kind() {
        return kind;
    }

    /**
     * 执行 Jdbc Dialect 相关业务数据，并返回处理结果。
     *
     * @param table 用于完成本次业务处理的 table 参数。
     * @param columns 用于完成本次业务处理的 columns 参数。
     * @param valuesSql 用于完成本次业务处理的 valuesSql 参数。
     * @param conflictColumns 用于完成本次业务处理的 conflictColumns 参数。
     * @param updateColumns 用于完成本次业务处理的 updateColumns 参数。
     * @return 返回 Jdbc Dialect 相关操作生成的结果数据。
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
     * 定义 Kind 可用的枚举值及其业务语义。
     */
    public enum Kind {
        H2,
        MYSQL,
        POSTGRESQL,
        UNKNOWN
    }
}
