package com.shiyu.ai.chat.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.ReactorHttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class HttpConfig {

    /**
     * 默认的 HTTP 客户端超时配置
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(120);
    
    /**
     * 连接池配置参数
     */
    private static final int MAX_CONNECTIONS = 100;
    private static final int ACQUIRE_TIMEOUT_SECONDS = 45;
    private static final int POOL_MAX_IDLE_TIME_SECONDS = 300;
    private static final int CONNECTION_POOL_CLEANUP_INTERVAL_SECONDS = 60;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(createClientHttpRequestFactory());
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = createReactorHttpClient();
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
    
    /**
     * 创建连接池配置
     */
    private ConnectionProvider createConnectionProvider() {
        return ConnectionProvider.builder("http-connection-pool")
                .maxConnections(MAX_CONNECTIONS)
                .pendingAcquireTimeout(Duration.ofSeconds(ACQUIRE_TIMEOUT_SECONDS))
                .maxIdleTime(Duration.ofSeconds(POOL_MAX_IDLE_TIME_SECONDS))
                .evictInBackground(Duration.ofSeconds(CONNECTION_POOL_CLEANUP_INTERVAL_SECONDS))
                .build();
    }

    /**
     * 创建同步 HTTP 客户端请求工厂
     */
    private ClientHttpRequestFactory createClientHttpRequestFactory() {
        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(CONNECT_TIMEOUT)
                .withReadTimeout(READ_TIMEOUT);
        return ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);
    }

    /**
     * 创建响应式 HTTP 客户端（带连接池）
     */
    private HttpClient createReactorHttpClient() {
        // 创建连接池
        ConnectionProvider connectionProvider = createConnectionProvider();
        // 使用连接池和配置创建 HttpClient
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(READ_TIMEOUT);
    }
}
