package com.shiyu.ai.web.common;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsValidationToHttpAndStableBusinessCodeWithoutLeakingDetails() {
        var response = handler.invalidArgument(new IllegalArgumentException("SQL table secret does not exist"));
        Result<Void> body = response.getBody();
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertEquals(BizResultCode.ERR_10007.getCode(), body.getCode());
        assertEquals(BizResultCode.ERR_10007.getMsg(), body.getMessage());
        assertFalse(body.isSuccess());
    }

    @Test
    void mapsTenantAccessAndNotFoundToStableCodes() {
        var forbidden = handler.invalidArgument(new IllegalArgumentException("access denied for tenant"));
        var missing = handler.invalidState(new IllegalStateException("resource not found"));
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        assertEquals(BizResultCode.FORBIDDEN.getCode(), forbidden.getBody().getCode());
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals(BizResultCode.NOT_FOUND.getCode(), missing.getBody().getCode());
    }

    @Test
    void hidesUnexpectedRuntimeDetails() {
        var response = handler.unexpected(new RuntimeException("jdbc password=secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(BizResultCode.ERROR.getCode(), response.getBody().getCode());
        assertEquals(BizResultCode.ERROR.getMsg(), response.getBody().getMessage());
    }

    @Test
    void preservesExplicitResponseStatusExceptions() {
        var response = handler.responseStatus(new ResponseStatusException(HttpStatus.CONFLICT, "generation was modified"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(BizResultCode.ERR_10008.getCode(), response.getBody().getCode());
    }
}
