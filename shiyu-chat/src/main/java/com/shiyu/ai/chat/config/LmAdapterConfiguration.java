package com.shiyu.ai.chat.config;

import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.platform.impl.GenericOpenAiAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LmAdapterConfiguration {

    @Bean("openAIModelAdapter")
    public GenericOpenAiAdapter openAIAdapter(PlatformProperties props) {
        return new GenericOpenAiAdapter(PlatformEnum.OPENAI,
                props.getOpenai().getBaseUrl(), props.getOpenai().getApiKey(), props.getOpenai().getModel());
    }

    @Bean("deepseekModelAdapter")
    public GenericOpenAiAdapter deepSeekAdapter(PlatformProperties props) {
        return new GenericOpenAiAdapter(PlatformEnum.DEEPSEEK,
                props.getDeepseek().getBaseUrl(), props.getDeepseek().getApiKey(), props.getDeepseek().getModel());
    }

    @Bean("openRouterModelAdapter")
    public GenericOpenAiAdapter openRouterAdapter(PlatformProperties props) {
        return new GenericOpenAiAdapter(PlatformEnum.OPEN_ROUTER,
                props.getOpenrouter().getBaseUrl(), props.getOpenrouter().getApiKey(), props.getOpenrouter().getModel());
    }

    @Bean("siliconFlowModelAdapter")
    public GenericOpenAiAdapter siliconFlowAdapter(PlatformProperties props) {
        return new GenericOpenAiAdapter(PlatformEnum.SILICON_FLOW,
                props.getSiliconflow().getBaseUrl(), props.getSiliconflow().getApiKey(), props.getSiliconflow().getModel());
    }
}