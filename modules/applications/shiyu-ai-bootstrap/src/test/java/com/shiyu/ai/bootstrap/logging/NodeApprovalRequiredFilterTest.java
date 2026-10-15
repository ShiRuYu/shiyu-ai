package com.shiyu.ai.bootstrap.logging;

import com.shiyu.ai.agent.implementation.runtime.service.AgentCancellationLogContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 验证 Log4j 对取消日志过滤器的配置与加载。 */
class NodeApprovalRequiredFilterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void configuresThreadAwareFilterForLangGraphCancellationErrors() throws Exception {
        Path source = Path.of(getClass().getClassLoader()
                .getResource("log4j2-spring.xml")
                .toURI());

        String contents = Files.readString(source);
        assertTrue(contents.contains("<AgentCancellationFilter/>"));
        assertFalse(contents.contains("packages="), "Log4j plugin discovery must use build metadata");
    }

    @Test
    void suppressesOnlyApprovalPauseErrorsFromLangGraphConfiguration() throws Exception {
        Path source = Path.of(getClass().getClassLoader()
                .getResource("log4j2-spring.xml")
                .toURI());
        Path config = temporaryDirectory.resolve("log4j2-test.xml");
        String contents = Files.readString(source)
                .replace("${sys:app.home:-.}", temporaryDirectory.toString())
                .replace("${spring:logging.level.com.shiyu:-INFO}", "INFO");
        Files.writeString(config, contents);

        LoggerContext context = Configurator.initialize(
                "approval-filter-test", getClass().getClassLoader(), config.toUri());
        try {
            LoggerConfig logger = context.getConfiguration().getLoggers().get("org.bsc.langgraph4j");
            assertNotNull(logger);
            Filter filter = logger.getFilter();
            assertNotNull(filter);

            assertEquals(Filter.Result.DENY, filter.filter(event(
                    Level.ERROR,
                    "com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException: tool approval is required")));
            assertEquals(Filter.Result.NEUTRAL, filter.filter(event(Level.ERROR, "graph node failed")));
            assertEquals(Filter.Result.ACCEPT, filter.filter(event(
                    Level.WARN,
                    "com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException: tool approval is required")));
        } finally {
            Configurator.shutdown(context);
        }
    }

    @Test
    void suppressesOnlyLangGraphInterruptsFromMarkedCancellationThreads() throws Exception {
        LoggerContext context = configuredContext("cancellation-filter-test");
        try {
            LoggerConfig logger = context.getConfiguration().getLoggers().get("org.bsc.langgraph4j");
            assertNotNull(logger);
            Filter filter = logger.getFilter();
            assertNotNull(filter);

            long threadId = Thread.currentThread().threadId();
            var interrupted = event(
                    Level.ERROR,
                    "org.bsc.langgraph4j",
                    "graph execution interrupted",
                    new RuntimeException("graph failed", new InterruptedException("cancelled")),
                    threadId);
            assertEquals(Filter.Result.NEUTRAL, filter.filter(interrupted));

            AgentCancellationLogContext.mark(Thread.currentThread());
            try {
                assertEquals(Filter.Result.DENY, filter.filter(interrupted));
                assertEquals(Filter.Result.DENY, filter.filter(event(
                        Level.ERROR,
                        "org.bsc.langgraph4j.graph.execution",
                        "graph execution interrupted",
                        new InterruptedException("cancelled"),
                        threadId)));
                assertEquals(Filter.Result.NEUTRAL, filter.filter(event(
                        Level.ERROR,
                        "org.bsc.langgraph4j",
                        "graph failed",
                        new IllegalStateException("not an interruption"),
                        threadId)));
                assertEquals(Filter.Result.NEUTRAL, filter.filter(event(
                        Level.ERROR,
                        "another.library",
                        "interrupted",
                        new InterruptedException("cancelled"),
                        threadId)));
            } finally {
                AgentCancellationLogContext.clear(Thread.currentThread());
            }
        } finally {
            Configurator.shutdown(context);
        }
    }

    private LoggerContext configuredContext(String name) throws Exception {
        Path source = Path.of(getClass().getClassLoader()
                .getResource("log4j2-spring.xml")
                .toURI());
        Path config = temporaryDirectory.resolve(name + ".xml");
        String contents = Files.readString(source)
                .replace("${sys:app.home:-.}", temporaryDirectory.toString())
                .replace("${spring:logging.level.com.shiyu:-INFO}", "INFO");
        Files.writeString(config, contents);
        return Configurator.initialize(name, getClass().getClassLoader(), config.toUri());
    }

    private static Log4jLogEvent event(Level level, String message) {
        return event(level, "org.bsc.langgraph4j", message, null, Thread.currentThread().threadId());
    }

    private static Log4jLogEvent event(
            Level level, String loggerName, String message, Throwable error, long threadId) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName(loggerName)
                .setLevel(level)
                .setMessage(new SimpleMessage(message))
                .setThrown(error)
                .setThreadId(threadId)
                .build();
    }
}
