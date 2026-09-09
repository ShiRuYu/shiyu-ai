package com.shiyu.ai.common.mybatis.handler;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MybatisExceptionHandlerTest {

    private final MybatisExceptionHandler handler = new MybatisExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void hidesDatabaseDriverDetailsFromGenericFailures() {
        var result = handler.handleCannotFindDataSourceException(
                new MyBatisSystemException(
                        "database operation failed",
                        new IllegalStateException("jdbc password=secret")), request);

        assertEquals("数据库操作失败，请稍后重试", result.getMessage());
        assertFalse(result.getMessage().contains("password"));
    }

    @Test
    void keepsStableMessageForMissingDataSource() {
        var result = handler.handleCannotFindDataSourceException(
                new MyBatisSystemException(
                        "CannotFindDataSourceException",
                        new IllegalStateException("CannotFindDataSourceException: secret")), request);

        assertEquals("未找到数据源，请联系管理员确认", result.getMessage());
        assertFalse(result.getMessage().contains("secret"));
    }
}
