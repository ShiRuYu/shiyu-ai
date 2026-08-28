package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.vo.CaptchaVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaServiceImplTest {
    @Test
    void generatedCaptchaIsSvgAndSingleUseCaseInsensitive() {
        CaptchaServiceImpl service = new CaptchaServiceImpl();
        CaptchaVO captcha = service.generateCaptcha();
        assertTrue(captcha.getKey().startsWith("captcha_"));
        assertTrue(captcha.getImage().startsWith("<svg"));
        assertEquals(300, captcha.getExpireTime());
        assertFalse(service.validateCaptcha(captcha.getKey(), null));
        assertFalse(service.validateCaptcha(captcha.getKey(), "wrong"));
        service.destroyCaptcha(captcha.getKey());
        assertFalse(service.validateCaptcha(captcha.getKey(), "wrong"));
    }

    @Test
    void rejectsMissingExpiredAndExhaustedEntriesAndAcceptsMatchingCode() throws Exception {
        CaptchaServiceImpl service = new CaptchaServiceImpl();
        Field storeField = CaptchaServiceImpl.class.getDeclaredField("captchaStore");
        storeField.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Object> store = (Map<String, Object>) storeField.get(service);
        Field attemptsField = CaptchaServiceImpl.class.getDeclaredField("attemptCount");
        attemptsField.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(service);
        Class<?> dataType = Class.forName("com.shiyu.ai.auth.service.impl.CaptchaServiceImpl$CaptchaData");
        Constructor<?> ctor = dataType.getDeclaredConstructor(String.class, long.class);
        ctor.setAccessible(true);

        assertFalse(service.validateCaptcha("missing", "abc"));
        store.put("expired", ctor.newInstance("abc", 0L));
        assertFalse(service.validateCaptcha("expired", "abc"));
        assertFalse(store.containsKey("expired"));
        store.put("valid", ctor.newInstance("AbC", System.currentTimeMillis() + 60_000));
        assertTrue(service.validateCaptcha("valid", "abc"));
        assertFalse(store.containsKey("valid"));
        store.put("exhausted", ctor.newInstance("abc", System.currentTimeMillis() + 60_000));
        attempts.put("exhausted", 3);
        assertFalse(service.validateCaptcha("exhausted", "abc"));
        assertFalse(store.containsKey("exhausted"));
    }
}
