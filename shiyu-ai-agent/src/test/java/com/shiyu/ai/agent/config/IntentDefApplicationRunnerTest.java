package com.shiyu.ai.agent.config;

import com.shiyu.ai.agent.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.dal.bo.agent.IntentDefBO;
import com.shiyu.ai.dal.repository.agent.IntentDefRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * IntentDefApplicationRunner 单元测试
 */
@Tag("dev")
@ExtendWith(MockitoExtension.class)
class IntentDefApplicationRunnerTest {

    @Mock
    private IntentDefRepository intentDefRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private IntentDefApplicationRunner runner;

    @Test
    void testRunWithData() {
        IntentDefBO bo = new IntentDefBO();
        bo.setId(1L);
        bo.setName("greeting");
        when(intentDefRepository.selectByAgentId("default")).thenReturn(List.of(bo));

        runner.run(applicationArguments);

        verify(intentDefRepository, times(1)).selectByAgentId("default");
    }

    @Test
    void testRunWithEmptyData() {
        when(intentDefRepository.selectByAgentId("default")).thenReturn(List.of());

        runner.run(applicationArguments);

        verify(intentDefRepository, times(1)).selectByAgentId("default");
    }

    @Test
    void testRunWithException() {
        when(intentDefRepository.selectByAgentId("default"))
                .thenThrow(new RuntimeException("DB error"));

        // Should not throw - caught by catch block
        runner.run(applicationArguments);

        verify(intentDefRepository, times(1)).selectByAgentId("default");
    }
}
