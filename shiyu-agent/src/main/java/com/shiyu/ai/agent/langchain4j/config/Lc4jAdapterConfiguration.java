package com.shiyu.ai.agent.langchain4j.config;

import com.shiyu.ai.agent.biz.agent.config.PlatformProperties;
import com.shiyu.ai.agent.langchain4j.impl.GenericLc4jAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Lc4jAdapterConfiguration {

    @Bean("lc4jOpenAIAdapter")
    public GenericLc4jAdapter openAIAdapter(PlatformProperties props) {
        return new GenericLc4jAdapter("OPENAI",
                props.getOpenai().getBaseUrl(), props.getOpenai().getApiKey(), props.getOpenai().getModel());
    }

    @Bean("lc4jDeepSeekAdapter")
    public GenericLc4jAdapter deepSeekAdapter(PlatformProperties props) {
        return new GenericLc4jAdapter("DEEPSEEK",
                props.getDeepseek().getBaseUrl(), props.getDeepseek().getApiKey(), props.getDeepseek().getModel());
    }

    @Bean("lc4jOpenRouterAdapter")
    public GenericLc4jAdapter openRouterAdapter(PlatformProperties props) {
        return new GenericLc4jAdapter("OPENROUTER",
                props.getOpenrouter().getBaseUrl(), props.getOpenrouter().getApiKey(), props.getOpenrouter().getModel());
    }

    @Bean("lc4jSiliconFlowAdapter")
    public GenericLc4jAdapter siliconFlowAdapter(PlatformProperties props) {
        return new GenericLc4jAdapter("SILICON_FLOW",
                props.getSiliconflow().getBaseUrl(), props.getSiliconflow().getApiKey(), props.getSiliconflow().getModel());
    }
}