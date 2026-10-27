package com.shiyu.ai.composition.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;
/** 验证 PostgreSQL 分句器保留字符串、标识符、注释和过程体内的分号。 */

class PostgresqlScriptExecutorTest {

    @Test
    void keepsDollarQuotedProceduralBodiesAsSingleStatements() {
        String script = "CREATE FUNCTION f() RETURNS void LANGUAGE plpgsql AS $body$ "
                + "BEGIN PERFORM 1; PERFORM 2; END; $body$; SELECT 3;";

        assertEquals(
                List.of(
                        "CREATE FUNCTION f() RETURNS void LANGUAGE plpgsql AS $body$ "
                                + "BEGIN PERFORM 1; PERFORM 2; END; $body$",
                        "SELECT 3"),
                PostgresqlScriptExecutor.splitStatements(script));
    }

    @Test
    void ignoresSemicolonsInsideStringsIdentifiersAndComments() {
        String script = "SELECT 'a;''b'; -- comment;\n"
                + "SELECT \"semi;\" /* outer; /* nested; */ done */;";

        assertEquals(
                List.of("SELECT 'a;''b'", "-- comment;\nSELECT \"semi;\" /* outer; /* nested; */ done */"),
                PostgresqlScriptExecutor.splitStatements(script));
    }

    @Test
    void doesNotTreatDollarInsideAnUnquotedIdentifierAsAQuote() {
        assertEquals(
                List.of("SELECT foo$bar$baz", "SELECT 1"),
                PostgresqlScriptExecutor.splitStatements("SELECT foo$bar$baz; SELECT 1;"));
        assertEquals(
                List.of("SELECT foo$$", "SELECT 2"),
                PostgresqlScriptExecutor.splitStatements("SELECT foo$$; SELECT 2;"));
    }

    @Test
    void doesNotTreatDollarAfterUnicodeIdentifierContinuationAsAQuote() {
        String script = "SELECT é$tag$; SELECT 2;";

        assertEquals(
                List.of("SELECT é$tag$", "SELECT 2"),
                PostgresqlScriptExecutor.splitStatements(script));
    }

    @Test
    void doesNotTreatAnIdentifierEndingInEAsAnEscapeStringPrefix() {
        assertEquals(
                List.of("SELECT custom_e'ends with backslash\\'", "SELECT 2"),
                PostgresqlScriptExecutor.splitStatements(
                        "SELECT custom_e'ends with backslash\\'; SELECT 2;"));
    }

    @Test
    void rejectsAnUnterminatedDollarQuote() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PostgresqlScriptExecutor.splitStatements("DO $$ BEGIN PERFORM 1;"));
    }
}
