package com.shiyu.ai.iam.implementation.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.vo.CaptchaVO;

import org.junit.jupiter.api.Test;

class CaptchaControllerCoverageTest {
    private final CaptchaService service = mock(CaptchaService.class);
    private final CaptchaController controller = new CaptchaController(service);

    @Test
    void mapsCaptchaGenerationAndValidationOutcomes() {
        when(service.generateCaptcha()).thenReturn(new CaptchaVO("key", "svg", 60L));
        assertTrue(controller.getCaptcha().isSuccess());
        when(service.validateCaptcha("key", "ok")).thenReturn(true);
        when(service.validateCaptcha("key", "bad")).thenReturn(false);
        assertTrue(
                controller
                        .validateCaptcha(new CaptchaController.ValidateCaptchaRequest("key", "ok"))
                        .getData()
                        .getSuccess());
        assertFalse(
                controller
                        .validateCaptcha(new CaptchaController.ValidateCaptchaRequest("key", "bad"))
                        .getData()
                        .getSuccess());
    }

    @Test
    void mapsCaptchaFailuresToStableResult() {
        when(service.generateCaptcha()).thenThrow(new IllegalStateException("down"));
        assertFalse(controller.getCaptcha().isSuccess());
        when(service.validateCaptcha(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("invalid"));
        assertFalse(
                controller
                        .validateCaptcha(new CaptchaController.ValidateCaptchaRequest("key", "bad"))
                        .isSuccess());
    }
}
