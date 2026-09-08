package com.shiyu.ai.common.web.interceptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WebInvokeInterceptorTest {

    private final WebInvokeInterceptor interceptor = new WebInvokeInterceptor();

    @Test
    void masksCompleteQuotedSensitiveValues() {
        String input = "{\"password\":\"abc,def\",\"content\":\"hello world\",\"safe\":\"keep\"}";

        String sanitized = interceptor.sanitize(input);

        assertEquals("{\"password\":\"***\",\"content\":\"***\",\"safe\":\"keep\"}", sanitized);
        assertFalse(sanitized.contains("abc,def"));
        assertFalse(sanitized.contains("hello world"));
    }

    @Test
    void masksEscapedQuotesInsideSensitiveValues() {
        String input = "{\"token\":\"secret \\\"value\\\"\",\"safe\":\"keep\"}";

        String sanitized = interceptor.sanitize(input);

        assertEquals("{\"token\":\"***\",\"safe\":\"keep\"}", sanitized);
        assertFalse(sanitized.contains("secret"));
    }

    @Test
    void masksApiKeyNamingVariants() {
        String input = "{\"apiKey\":\"key-one\",\"api-key\":\"key-two\",\"api_key\":\"key-three\",\"safe\":\"keep\"}";

        String sanitized = interceptor.sanitize(input);

        assertEquals("{\"apiKey\":\"***\",\"api-key\":\"***\",\"api_key\":\"***\",\"safe\":\"keep\"}", sanitized);
        assertFalse(sanitized.contains("key-one"));
        assertFalse(sanitized.contains("key-two"));
        assertFalse(sanitized.contains("key-three"));
    }

    @Test
    void masksAccessAndRefreshCredentialVariants() {
        String input = "{\"accessToken\":\"access-value\",\"refreshToken\":\"refresh-value\",\"clientSecret\":\"client-value\",\"safe\":\"keep\"}";

        String sanitized = interceptor.sanitize(input);

        assertEquals("{\"accessToken\":\"***\",\"refreshToken\":\"***\",\"clientSecret\":\"***\",\"safe\":\"keep\"}", sanitized);
        assertFalse(sanitized.contains("access-value"));
        assertFalse(sanitized.contains("refresh-value"));
        assertFalse(sanitized.contains("client-value"));
    }
}
