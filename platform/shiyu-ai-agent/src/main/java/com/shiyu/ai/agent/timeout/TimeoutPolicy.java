package com.shiyu.ai.agent.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 超时策略
 */
public interface TimeoutPolicy {

    <T> T executeWithTimeout(Callable<T> callable, TimeoutConfig config) throws Exception;
}
