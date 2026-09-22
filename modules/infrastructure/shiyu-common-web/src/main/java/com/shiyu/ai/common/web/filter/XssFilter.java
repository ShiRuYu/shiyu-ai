package com.shiyu.ai.common.web.filter;

import com.shiyu.ai.common.foundation.CharConstants;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 承载 Xss Filter 所属 Web 能力的请求适配和边界处理。
 */
public class XssFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public List<String> excludes = new ArrayList<>();

    /**
     * 执行 Xss Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param filterConfig 用于完成本次业务处理的 filterConfig 参数。
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes)) {
            String[] url = tempExcludes.split(CharConstants.COMMA);
            for (int i = 0; url != null && i < url.length; i++) {
                excludes.add(url[i]);
            }
        }
    }

    /**
     * 执行 Xss Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param response 用于完成本次业务处理的 response 参数。
     * @param chain 用于完成本次业务处理的 chain 参数。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest =
                new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        return excludes.stream().anyMatch(pattern -> pathMatcher.match(pattern, url));
    }

    /**
     * 执行 Xss Filter 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public void destroy() {}
}
