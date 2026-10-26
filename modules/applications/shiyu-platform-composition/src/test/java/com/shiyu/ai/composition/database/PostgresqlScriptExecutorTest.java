package com.shiyu.ai.composition.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

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
    void rejectsAnUnterminatedDollarQuote() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PostgresqlScriptExecutor.splitStatements("DO $$ BEGIN PERFORM 1;"));
    }
}
