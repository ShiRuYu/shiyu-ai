package com.shiyu.ai.governance.implementation.usage;

import com.shiyu.ai.governance.implementation.usage.port.repository.PlatformUsageRepository;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.service.PlatformUsageService;
import com.shiyu.ai.governance.implementation.usage.port.UsageService;
import com.shiyu.ai.governance.implementation.usage.service.impl.UsageServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class PlatformUsageWiringTest {
    @Test
    void tenantServiceRemainsUnambiguousWhenPlatformServiceIsRegistered() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("tenantRepository", UsageRecordRepository.class,
                    () -> mock(UsageRecordRepository.class), definition -> definition.setPrimary(true));
            context.registerBean(PlatformUsageRepository.class, () -> mock(PlatformUsageRepository.class));
            context.register(UsageServiceImpl.class, PlatformUsageService.class);
            context.refresh();
            assertInstanceOf(UsageServiceImpl.class, context.getBean(UsageService.class));
        }
    }
}
