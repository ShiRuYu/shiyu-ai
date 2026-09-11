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
 * Small SQL dialect adapter for the few write operations that cannot use one portable statement
 * across H2, MySQL, and PostgreSQL.
 *
 * <p>Read queries remain ANSI SQL. Callers provide validated table and column identifiers; values
 * stay parameterized in the returned statement.
 */
public final class JdbcDialect {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    private final Kind kind;

    private JdbcDialect(Kind kind) {
        this.kind = kind;
    }

    public static JdbcDialect detect(JdbcTemplate jdbc) {
        Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        DataSource dataSource = jdbc.getDataSource();
        if (dataSource == null) return new JdbcDialect(Kind.H2);
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return fromProduct(product);
        } catch (SQLException | RuntimeException ignored) {
            // Unit tests often use a disconnected mock DataSource. H2 is the
            // safe compatibility fallback and preserves the existing statement.
            return new JdbcDialect(Kind.H2);
        }
    }

    public static JdbcDialect fromProduct(String product) {
        String normalized = product == null ? "" : product.toLowerCase(Locale.ROOT);
        if (normalized.contains("postgres")) return new JdbcDialect(Kind.POSTGRESQL);
        if (normalized.contains("mysql")) return new JdbcDialect(Kind.MYSQL);
        if (normalized.contains("h2")) return new JdbcDialect(Kind.H2);
        return new JdbcDialect(Kind.UNKNOWN);
    }

    public Kind kind() {
        return kind;
    }

    /**
     * Builds an insert-or-update statement while preserving the existing H2 syntax. The returned
     * SQL contains only identifier fragments and `?` placeholders supplied by the caller.
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

    /** Returns a retry timestamp expression using the supplied attempts column. */
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

    public enum Kind {
        H2,
        MYSQL,
        POSTGRESQL,
        UNKNOWN
    }
}
