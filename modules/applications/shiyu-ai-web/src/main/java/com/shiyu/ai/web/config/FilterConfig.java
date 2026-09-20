package com.shiyu.ai.web.config;

import com.shiyu.ai.common.core.CharConstants;
import com.shiyu.ai.common.web.filter.RepeatableFilter;
import com.shiyu.ai.common.web.filter.XssFilter;
import com.shiyu.ai.web.config.properties.XssProperties;

import jakarta.servlet.DispatcherType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义 Filter 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
public class FilterConfig {

    /**
     * 执行 Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param rawtypes 用于完成本次业务处理的 rawtypes 参数。
     * @param unchecked 用于完成本次业务处理的 unchecked 参数。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    public FilterRegistrationBean xssFilterRegistration(XssProperties xssProperties) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns(
                StringUtils.split(xssProperties.getUrlPatterns(), CharConstants.COMMA));
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", xssProperties.getExcludes());
        registration.setInitParameters(initParameters);
        return registration;
    }

    /**
     * 执行 Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param rawtypes 用于完成本次业务处理的 rawtypes 参数。
     * @param unchecked 用于完成本次业务处理的 unchecked 参数。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatableFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE);
        return registration;
    }
}
