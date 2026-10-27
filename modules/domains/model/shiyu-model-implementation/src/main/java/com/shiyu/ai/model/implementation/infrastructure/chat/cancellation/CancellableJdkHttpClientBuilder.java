package com.shiyu.ai.model.implementation.infrastructure.chat.cancellation;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;

import java.time.Duration;

/** 创建使用可取消服务端事件流传输的 JDK HTTP 客户端。 */
public final class CancellableJdkHttpClientBuilder implements HttpClientBuilder {
    private final JdkHttpClientBuilder delegate =
            new JdkHttpClientBuilder().httpClientBuilder(java.net.http.HttpClient.newBuilder());

    @Override
    public Duration connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public HttpClientBuilder connectTimeout(Duration timeout) {
        delegate.connectTimeout(timeout);
        return this;
    }

    @Override
    public Duration readTimeout() {
        return delegate.readTimeout();
    }

    @Override
    public HttpClientBuilder readTimeout(Duration timeout) {
        delegate.readTimeout(timeout);
        return this;
    }

    @Override
    public HttpClient build() {
        return new CancellableJdkHttpClient(
                new JdkHttpClient(delegate),
                delegate.httpClientBuilder().build(),
                delegate.readTimeout());
    }
}
