import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 在单个事务中回填仓库的 H2 教育演示种子数据。 */
public class BackfillDemoEducation {
    private static final Pattern TABLE = Pattern.compile("(?i)INSERT\\s+INTO\\s+\"PUBLIC\"\\.\"([A-Z_]+)\"");
    private static final List<String> FILES = List.of(
            "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/seed/knowledge/06_demo_content.sql",
            "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/07_education.sql",
            "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/08_learning_progress.sql",
            "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/09_curriculum.sql",
            "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/10_resources.sql",
            "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/11_resource_variants.sql");

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: BackfillDemoEducation <repository root> <H2 file without .mv.db>");
        }
        Path root = Path.of(args[0]).toRealPath();
        Path database = Path.of(args[1]).toAbsolutePath();
        if (!Files.isRegularFile(Path.of(database + ".mv.db"))) {
            throw new IllegalArgumentException("Existing H2 database required: " + database);
        }
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:file:" + database.toString().replace('\\', '/') + ";MODE=MySQL;IFEXISTS=TRUE", "sa", "")) {
            connection.setAutoCommit(false);
            try {
                if (countWhere(connection, "EDU_STUDENT",
                        "ID=1 AND TENANT_ID=1 AND USER_ID=2 AND STUDENT_NO='DEMO-2026-001'") == 1
                        && countWhere(connection, "EDU_SUBJECT", "ID=1 AND CODE='MATH'") == 1
                        && countWhere(connection, "EDU_QUESTION", "ID IN (1,2)") == 2
                        && countWhere(connection, "EDU_STUDY_RECORD", "ID IN (1,2) AND STUDENT_ID=1") == 2
                        && countWhere(connection, "KNOWLEDGE_BASE",
                                "ID=1001 AND CODE='MATH_LINEAR_EQUATION'") == 1) {
                    ensureEducationSpace(connection, root);
                    connection.commit();
                    System.out.println("Demo education seed present; education space verified.");
                    return;
                }
                for (String table : List.of("EDU_STUDENT", "EDU_SUBJECT", "EDU_QUESTION",
                        "EDU_STUDY_RECORD", "EDU_TEACHER", "EDU_COURSE", "EDU_RESOURCE",
                        "KNOWLEDGE_BASE", "KNOWLEDGE_DOCUMENT", "VECTOR_KNOWLEDGE_CHUNK")) {
                    if (count(connection, table) != 0) {
                        throw new IllegalStateException(table + " contains data; refusing a partial demo backfill");
                    }
                }
                if (countWhere(connection, "KNOWLEDGE_SPACE", "ID=1 AND TENANT_ID=1") != 1) {
                    throw new IllegalStateException("Expected tenant 1 knowledge space 1 is missing");
                }
                int statements = 0;
                for (String file : FILES) {
                    String sql = Files.readString(root.resolve(file), StandardCharsets.UTF_8);
                    for (String raw : sql.split(";")) {
                        String statement = raw.replaceFirst("(?s)^\\s*(?:--[^\\r\\n]*(?:\\r?\\n|$)\\s*)*", "").trim();
                        if (statement.isEmpty()) continue;
                        Matcher matcher = TABLE.matcher(statement);
                        if (!matcher.find()) throw new IllegalStateException("Unexpected seed statement in " + file);
                        if (matcher.group(1).equalsIgnoreCase("KNOWLEDGE_SPACE")) continue;
                        try (Statement command = connection.createStatement()) {
                            command.executeUpdate(statement);
                        }
                        statements++;
                    }
                }
                if (count(connection, "EDU_STUDENT") != 1 || count(connection, "EDU_QUESTION") != 2
                        || count(connection, "EDU_STUDY_RECORD") != 2 || count(connection, "KNOWLEDGE_BASE") != 1) {
                    throw new IllegalStateException("Demo seed postcondition failed");
                }
                ensureEducationSpace(connection, root);
                connection.commit();
                System.out.println("Committed " + statements + " demo education seed statements.");
            } catch (Exception failure) {
                connection.rollback();
                throw failure;
            }
        }
    }

    private static int count(Connection connection, String table) throws Exception {
        return countWhere(connection, table, "1=1");
    }

    private static void ensureEducationSpace(Connection connection, Path root) throws Exception {
        if (countWhere(connection, "KNOWLEDGE_SPACE", "ID=1001") == 0) {
            String seed = Files.readString(root.resolve(FILES.getFirst()), StandardCharsets.UTF_8);
            String statement = seed.substring(0, seed.indexOf(';')).replaceFirst("VALUES \\(1,", "VALUES (1001,");
            try (Statement command = connection.createStatement()) {
                command.executeUpdate(statement);
            }
        }
        if (countWhere(connection, "KNOWLEDGE_SPACE",
                "ID=1001 AND TENANT_ID=1 AND DOMAIN_CODE='EDUCATION' AND ACCESS_MODE='TENANT'") != 1) {
            throw new IllegalStateException("Education knowledge space 1001 conflicts with existing data");
        }
        for (String table : List.of("KNOWLEDGE_BASE", "KNOWLEDGE_DOCUMENT",
                "KNOWLEDGE_DOCUMENT_VERSION", "KNOWLEDGE_DOC_RELATION", "VECTOR_KNOWLEDGE_CHUNK",
                "KNOWLEDGE_INGESTION_JOB", "KNOWLEDGE_AUDIT_LOG", "KNOWLEDGE_EVALUATION_CASE")) {
            try (Statement command = connection.createStatement()) {
                command.executeUpdate("UPDATE " + table + " SET SPACE_ID=1001 WHERE ID=1001 AND SPACE_ID=1");
            }
        }
        if (countWhere(connection, "KNOWLEDGE_BASE", "ID=1001 AND SPACE_ID=1001") != 1) {
            throw new IllegalStateException("Demo point is not attached to education space");
        }
    }

    private static int countWhere(Connection connection, String table, String condition) throws Exception {
        try (Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + condition)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
