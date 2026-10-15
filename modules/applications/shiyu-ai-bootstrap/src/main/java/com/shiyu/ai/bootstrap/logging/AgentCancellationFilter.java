package com.shiyu.ai.bootstrap.logging;

import com.shiyu.ai.agent.implementation.runtime.service.AgentCancellationLogContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

/** 过滤 LangGraph 中因工作线程取消而产生的 error 级异常堆栈日志。 */
@Plugin(
        name = "AgentCancellationFilter",
        category = Core.CATEGORY_NAME,
        elementType = Filter.ELEMENT_TYPE,
        printObject = true)
public final class AgentCancellationFilter extends AbstractFilter {

    private static final String LANGGRAPH_LOGGER = "org.bsc.langgraph4j";

    private AgentCancellationFilter() {}

    /** 创建由 Log4j 配置文件使用的过滤器实例。 */
    @PluginFactory
    public static AgentCancellationFilter createFilter() {
        return new AgentCancellationFilter();
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null
                || !isLangGraphLogger(event.getLoggerName())
                || !Level.ERROR.equals(event.getLevel())
                || !AgentCancellationLogContext.isMarked(event.getThreadId())
                || !hasInterruptedException(event.getThrown())) {
            return Result.NEUTRAL;
        }
        return Result.DENY;
    }

    private static boolean isLangGraphLogger(String loggerName) {
        return LANGGRAPH_LOGGER.equals(loggerName)
                || (loggerName != null && loggerName.startsWith(LANGGRAPH_LOGGER + "."));
    }

    private static boolean hasInterruptedException(Throwable error) {
        int remaining = 32;
        for (Throwable current = error; current != null && remaining-- > 0; current = current.getCause()) {
            if (current instanceof InterruptedException) return true;
            if (current.getCause() == current) return false;
        }
        return false;
    }
}
