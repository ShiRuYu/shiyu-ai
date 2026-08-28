package com.shiyu.ai.agent.evaluation;

import com.shiyu.ai.application.db.DatabaseInitializer;
import com.shiyu.ai.kernel.context.TenantId;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcEvaluationRepositoryTest {
    @Test
    void persistsDatasetCasesAndRunResultsInH2() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:eval_" + UUID.randomUUID().toString().replace("-", "") + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        new DatabaseInitializer(Map.of("agent", dataSource), new StaticApplicationContext()).initialize();
        JdbcEvaluationRepository repository = new JdbcEvaluationRepository(dataSource);

        EvalDataset dataset = new EvalDataset("dataset-1", 1, 2, "smoke", "test", Instant.now());
        EvalCase evalCase = new EvalCase("case-1", dataset.id(), 1, "input", "expected", Map.of("budget", 10), Instant.now());
        EvalRun run = new EvalRun("run-1", dataset.id(), 1, 2, "version-1", EvalMetric.EXACT_MATCH, "COMPLETED", 1.0,
                List.of(new EvalResult(evalCase.id(), EvalMetric.EXACT_MATCH, 1.0, true, "passed")), Instant.now(), Instant.now());
        repository.insertDataset(dataset);
        repository.insertCase(evalCase);
        repository.insertRun(run);

        TenantId tenant = new TenantId(1);
        assertTrue(repository.findDataset(dataset.id(), tenant, 2).isPresent());
        assertEquals(1, repository.listCases(dataset.id(), tenant).size());
        assertEquals(run.results(), repository.findRun(run.id(), tenant, 2).orElseThrow().results());
    }
}
