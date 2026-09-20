package com.shiyu.ai.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Strings;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * 承载 Repeatable Filter 所属 Web 能力的请求适配和边界处理。
 */
public class RepeatableFilter implements Filter {
    /**
     * 执行 Repeatable Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param filterConfig 用于完成本次业务处理的 filterConfig 参数。
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    /**
     * 执行 Repeatable Filter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param response 用于完成本次业务处理的 response 参数。
     * @param chain 用于完成本次业务处理的 chain 参数。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest httpServletRequest) {
            String requestUri = httpServletRequest.getRequestURI();
            // 排除静态资源请求，避免对这些请求进行包装
            if (isStaticResourceRequest(requestUri)) {
                chain.doFilter(request, response);
                return;
            }
            // 只对Content-Type为JSON的请求进行包装
            if (Strings.CI.startsWith(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
                requestWrapper = new RepeatedlyRequestWrapper(httpServletRequest, response);
            }
        }
        if (null == requestWrapper) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    /**
     * 执行 Repeatable Filter 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public void destroy() {}

    /** ● 判断是否为静态资源请求 ● ● @param requestUri 请求URI ● @return 是否为静态资源请求 */
    private boolean isStaticResourceRequest(String requestUri) {
        return requestUri.endsWith("/favicon.ico")
                || requestUri.endsWith(".css")
                || requestUri.endsWith(".js")
                || requestUri.endsWith(".png")
                || requestUri.endsWith(".jpg")
                || requestUri.endsWith(".jpeg")
                || requestUri.endsWith(".gif")
                || requestUri.endsWith(".bmp")
                || requestUri.endsWith(".svg")
                || requestUri.endsWith(".ico")
                || requestUri.endsWith(".woff")
                || requestUri.endsWith(".woff2")
                || requestUri.endsWith(".ttf")
                || requestUri.endsWith(".eot")
                || requestUri.endsWith(".otf")
                || requestUri.endsWith(".map");
    }
}
