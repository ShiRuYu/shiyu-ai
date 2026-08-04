package com.shiyu.ai.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Repeatable 过滤器
 */
public class RepeatableFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest httpServletRequest) {
            String requestUri = httpServletRequest.getRequestURI();
            // 排除静态资源请求，避免对这些请求进行包装
            if (isStaticResourceRequest(requestUri)) {
                chain.doFilter(request, response);
                return;
            }
            // 只对Content-Type为JSON的请求进行包装
            if (StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
                requestWrapper = new RepeatedlyRequestWrapper(httpServletRequest, response);
            }
        }
        if (null == requestWrapper) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * ●  判断是否为静态资源请求
     * ●
     * ●  @param requestUri 请求URI
     * ●  @return 是否为静态资源请求
     */
    private boolean isStaticResourceRequest(String requestUri) {
        return requestUri.endsWith("/favicon.ico") || requestUri.endsWith(".css") ||
                requestUri.endsWith(".js") ||
                requestUri.endsWith(".png") ||
                requestUri.endsWith(".jpg") ||
                requestUri.endsWith(".jpeg") ||
                requestUri.endsWith(".gif") ||
                requestUri.endsWith(".bmp") ||
                requestUri.endsWith(".svg") ||
                requestUri.endsWith(".ico") ||
                requestUri.endsWith(".woff") ||
                requestUri.endsWith(".woff2") ||
                requestUri.endsWith(".ttf") ||
                requestUri.endsWith(".eot") ||
                requestUri.endsWith(".otf") ||
                requestUri.endsWith(".map");
    }
}
