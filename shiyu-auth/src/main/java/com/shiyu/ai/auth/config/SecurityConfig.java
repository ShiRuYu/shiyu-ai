package com.shiyu.ai.auth.config;

import com.shiyu.ai.auth.filter.JwtAuthenticationFilter;
import com.shiyu.ai.auth.handler.CustomAccessDeniedHandler;
import com.shiyu.ai.auth.handler.CustomAuthenticationEntryPoint;
import com.shiyu.ai.auth.service.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, 
                                                    CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                                    CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http
                // 禁用 CSRF（适用于无状态的 REST API）
                .csrf(AbstractHttpConfigurer::disable)
                //H2 Console 是 iframe + frameset 页面，而 Spring Boot / Spring Security 默认禁止 iframe。
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                // 配置授权规则
                .authorizeHttpRequests((authorize) -> authorize
                        // 允许匿名访问的接口
                        .requestMatchers("/auth/login").permitAll()
                        // Swagger/OpenAPI 文档接口
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/doc.html", "/swagger-resources/**").permitAll()
                        // 静态资源
                        .requestMatchers("/*.css", "/*.js", "/*.ico", "/webjars/**", "/static/**", "/public/**", "/resources/**", "/assets/**").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 使用无状态会话（不使用 Session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 禁用 HTTP Basic 认证（使用自定义登录接口）
                .httpBasic(Customizer.withDefaults())
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用表单登录（使用 REST 风格的登录接口）
                //.formLogin(Customizer.withDefaults())
                // 禁用登出
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
