package com.shiyu.ai.model.implementation.persistence.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelSchemaH2Test {

    @Test
    void modelTablesSupportCrudAndTenantFiltering() throws Exception {
        DataSource dataSource = dataSource();
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(
                    "db/baseline/h2/schema/model/04_model.sql"));
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO MODEL_AI_PLATFORM (ID,NAME,CODE,TENANT_ID) "
                        + "VALUES (1,'Tenant One','ONE',1)");
                statement.executeUpdate("INSERT INTO MODEL_AI_PLATFORM (ID,NAME,CODE,TENANT_ID) "
                        + "VALUES (2,'Tenant Two','TWO',2)");
                statement.executeUpdate("INSERT INTO MODEL_AI_MODEL (ID,PLATFORM_ID,MODEL_NAME,TENANT_ID) "
                        + "VALUES (1,1,'model-one',1)");
                statement.executeUpdate("INSERT INTO MODEL_AI_MODEL (ID,PLATFORM_ID,MODEL_NAME,TENANT_ID) "
                        + "VALUES (2,2,'model-two',2)");
                assertEquals(1, count(statement, "MODEL_AI_PLATFORM", 1));
                assertEquals(1, count(statement, "MODEL_AI_MODEL", 1));
                assertEquals(1, statement.executeUpdate(
                        "UPDATE MODEL_AI_MODEL SET DISPLAY_NAME='updated' "
                                + "WHERE ID=1 AND TENANT_ID=1"));
                assertEquals(0, statement.executeUpdate(
                        "UPDATE MODEL_AI_MODEL SET DISPLAY_NAME='cross-tenant' "
                                + "WHERE ID=2 AND TENANT_ID=1"));
                assertEquals(1, statement.executeUpdate(
                        "DELETE FROM MODEL_AI_MODEL WHERE ID=1 AND TENANT_ID=1"));
                assertEquals(0, count(statement, "MODEL_AI_MODEL", 1));
                assertEquals(1, count(statement, "MODEL_AI_MODEL", 2));
                assertEquals(0, countTables(statement, "AGENT_AI_" + "PLATFORM"));
                assertEquals(0, countTables(statement, "AGENT_AI_" + "MODEL"));
            }
        }
    }

    private long count(Statement statement, String table, long tenantId) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE TENANT_ID=" + tenantId;
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private long countTables(Statement statement, String table) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='" + table + "'")) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:model_schema;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
