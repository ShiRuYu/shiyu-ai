package com.shiyu.ai.common.web.filter;

import cn.hutool.core.io.IoUtil;

import com.shiyu.ai.common.foundation.CharConstants;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 承载 Repeatedly Request Wrapper 所属 Web 能力的请求适配和边界处理。
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {
    /**
     * body 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final byte[] body;

    /**
     * 执行 Repeatedly Request Wrapper 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param response 用于完成本次业务处理的 response 参数。
     */
    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response)
            throws IOException {
        super(request);
        request.setCharacterEncoding(CharConstants.UTF8);
        response.setCharacterEncoding(CharConstants.UTF8);

        body = IoUtil.readBytes(request.getInputStream(), false);
    }

    /**
     * 查询 Repeatedly Request Wrapper 相关业务数据，并返回处理结果。
     *
     * @return 返回 Repeatedly Request Wrapper 相关操作生成的结果数据。
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 查询 Repeatedly Request Wrapper 相关业务数据，并返回处理结果。
     *
     * @return 返回 Repeatedly Request Wrapper 相关操作生成的结果数据。
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            /**
             * {@code read} 执行当前类型定义的业务操作。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            /**
             * {@code available} 执行当前类型定义的业务操作。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public int available() throws IOException {
                return body.length;
            }

            /**
             * {@code isFinished} 校验当前操作的输入或状态是否满足约束。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public boolean isFinished() {
                return false;
            }

            /**
             * {@code isReady} 校验当前操作的输入或状态是否满足约束。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public boolean isReady() {
                return false;
            }

            /**
             * {@code setReadListener} 写入或更新当前模块中的业务数据。
             *
             * @param readListener 参数值，用于执行当前操作。
             */
            @Override
            public void setReadListener(ReadListener readListener) {}
        };
    }
}
