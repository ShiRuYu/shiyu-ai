package com.shiyu.ai.common.web.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** XSS过滤处理 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 处理xsshttpservlet请求wrapper。
     *
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * {@code getParameterValues} 查询并返回当前操作所需的数据。
     *
     * @param name 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapseValues = new String[length];
            for (int i = 0; i < length; i++) {
                // 防xss攻击和过滤前后空格
                escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
            }
            return escapseValues;
        }
        return super.getParameterValues(name);
    }

    /**
     * {@code getInputStream} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 非json类型，直接返回
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        // 为空，直接返回
        String json =
                StrUtil.str(
                        IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
        if (json == null || json.isEmpty()) {
            return super.getInputStream();
        }

        // xss过滤
        json = HtmlUtil.cleanHtmlTag(json).trim();
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream bis = IoUtil.toStream(jsonBytes);
        return new ServletInputStream() {
            /**
             * {@code isFinished} 校验当前操作的输入或状态是否满足约束。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public boolean isFinished() {
                return true;
            }

            /**
             * {@code isReady} 校验当前操作的输入或状态是否满足约束。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public boolean isReady() {
                return true;
            }

            /**
             * {@code available} 执行当前类型定义的业务操作。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public int available() throws IOException {
                return jsonBytes.length;
            }

            /**
             * {@code setReadListener} 写入或更新当前模块中的业务数据。
             *
             * @param readListener 参数值，用于执行当前操作。
             */
            @Override
            public void setReadListener(ReadListener readListener) {}

            /**
             * {@code read} 执行当前类型定义的业务操作。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
    }

    /** 是否是Json请求 */
    public boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return Strings.CI.startsWith(header, MediaType.APPLICATION_JSON_VALUE);
    }
}
