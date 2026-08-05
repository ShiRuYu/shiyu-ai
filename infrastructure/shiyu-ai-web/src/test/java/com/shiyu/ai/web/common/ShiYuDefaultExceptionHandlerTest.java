package com.shiyu.ai.web.common;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.ShiYuDefaultExceptionHandler;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("dev")
class ShiYuDefaultExceptionHandlerTest {

    private final ShiYuDefaultExceptionHandler handler = new ShiYuDefaultExceptionHandler();

    @Test
    void mapsUncodedBusinessExceptionsToBusinessFailure() {
        Result<String> result = handler.exception(new ServiceException("resource does not exist"));

        assertEquals(BizResultCode.ERR_10009.getCode(), result.getCode());
        assertEquals("resource does not exist", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void preservesExplicitBusinessCodes() {
        Result<String> result = handler.exception(new ServiceException("relation invalid", 2001));

        assertEquals(2001, result.getCode());
        assertEquals("relation invalid", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void mapsResponseStatusExceptionsWithoutTurningThemIntoInternalErrors() {
        Result<String> result = handler.exception(new ResponseStatusException(HttpStatus.NOT_FOUND, "resource absent"));

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getCode());
        assertEquals("resource absent", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void mapsHandledFailureMessagesToBusinessErrors() {
        Result<Void> result = Result.fail("handled failure");

        assertEquals(BizResultCode.ERR_10009.getCode(), result.getCode());
        assertEquals("handled failure", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void retainsInternalErrorForUnspecifiedFailures() {
        Result<Void> result = Result.fail();

        assertEquals(BizResultCode.ERROR.getCode(), result.getCode());
        assertFalse(result.isSuccess());
    }

}
