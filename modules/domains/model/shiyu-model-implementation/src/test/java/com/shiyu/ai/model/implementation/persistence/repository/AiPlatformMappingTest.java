package com.shiyu.ai.model.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBOToAiPlatformDOMapperImpl;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDO;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDOToAiPlatformBOMapperImpl;
import org.junit.jupiter.api.Test;

/** 验证平台协议经过持久化映射往返后保持不变。 */
class AiPlatformMappingTest {
    @Test
    void keepsSelectedAdapterTypeAcrossPersistenceMapping() throws Exception {
        AiPlatformBO platform = new AiPlatformBO();
        platform.setAdapterType("OLLAMA");

        AiPlatformDO stored = new AiPlatformBOToAiPlatformDOMapperImpl().convert(platform);
        Object persistedProtocol = AiPlatformDO.class.getMethod("getAdapterType").invoke(stored);
        assertEquals("OLLAMA", persistedProtocol);
        assertEquals("OLLAMA", new AiPlatformDOToAiPlatformBOMapperImpl()
                .convert(stored).getAdapterType());
    }

    @Test
    void keepsPlatformCredentialAcrossPersistenceMapping() throws Exception {
        AiPlatformBO platform = new AiPlatformBO();
        platform.setApiKey("mapping-test-key");

        AiPlatformDO stored = new AiPlatformBOToAiPlatformDOMapperImpl().convert(platform);
        assertEquals("mapping-test-key", AiPlatformDO.class.getMethod("getApiKey").invoke(stored));
        assertEquals("mapping-test-key", new AiPlatformDOToAiPlatformBOMapperImpl()
                .convert(stored).getApiKey());
    }
}
