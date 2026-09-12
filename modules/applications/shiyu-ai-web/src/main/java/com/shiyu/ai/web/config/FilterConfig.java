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

/** Filter配置 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
public class FilterConfig {

    /**
     * {@code xssFilterRegistration} 执行当前类型定义的业务操作。
     *
     * @param xssProperties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code someFilterRegistration} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
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
