package com.shiyu.ai.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shiyu.ai.web.common.ApiExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证数据库异常响应不会向客户端暴露连接信息，并保留数据源缺失提示。
 */
@Tag("dev")
@Tag("prod")
class MybatisExceptionHandlerTest {

    private final MybatisExceptionHandler handler = new MybatisExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void hidesDatabaseDriverDetailsFromGenericFailures() {
        var result =
                handler.handleCannotFindDataSourceException(
                        new MyBatisSystemException(
                                "database operation failed",
                                new IllegalStateException("jdbc password=secret")),
                        request);

        assertEquals("数据库操作失败，请稍后重试", result.getMessage());
        assertFalse(result.getMessage().contains("password"));
    }

    @Test
    void keepsStableMessageForMissingDataSource() {
        var result =
                handler.handleCannotFindDataSourceException(
                        new MyBatisSystemException(
                                "CannotFindDataSourceException",
                                new IllegalStateException("CannotFindDataSourceException: secret")),
                        request);

        assertEquals("未找到数据源，请联系管理员确认", result.getMessage());
        assertFalse(result.getMessage().contains("secret"));
    }

    @Test
    void usesDatabaseAdviceBeforeCatchAllApiAdvice() throws Exception {
        MockMvcBuilders.standaloneSetup(new DuplicateKeyController())
                .setControllerAdvice(new ApiExceptionHandler(), new MybatisExceptionHandler())
                .build()
                .perform(get("/duplicate-key-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("数据库中已存在该记录，请联系管理员确认"));
    }

    /** 为异常处理器优先级测试抛出数据库唯一约束异常。 */
    @RestController
    private static class DuplicateKeyController {

        @GetMapping("/duplicate-key-test")
        public void duplicateKey() {
            throw new DuplicateKeyException("database detail must stay private");
        }
    }
}
