package com.shiyu.ai.chat.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.ReactorHttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class HttpConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        JdkClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.jdk().build();
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder()
                .requestFactory(requestFactory);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(10000))
                .withReadTimeout(Duration.ofSeconds(120));

        ReactorHttpClientBuilder builder = new ReactorHttpClientBuilder();
        HttpClient httpClient = builder.build(httpClientSettings);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
