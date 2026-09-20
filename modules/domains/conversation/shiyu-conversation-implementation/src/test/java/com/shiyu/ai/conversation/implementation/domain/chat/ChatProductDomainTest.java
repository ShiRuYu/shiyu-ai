package com.shiyu.ai.conversation.implementation.domain.chat;

import com.shiyu.ai.conversation.implementation.domain.chat.model.LorebookEntry;

import com.shiyu.ai.conversation.implementation.domain.chat.service.PromptAssemblyService;

import com.shiyu.ai.conversation.implementation.domain.chat.model.GroupChat;

import com.shiyu.ai.conversation.implementation.domain.chat.model.SpeakerPolicy;

import com.shiyu.ai.conversation.implementation.domain.chat.service.GroupTurnPlanner;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.conversation.contract.api.*;
import com.shiyu.ai.conversation.contract.model.*;
import com.shiyu.ai.conversation.implementation.application.*;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.*;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 验证 对话 Product Domain 相关功能、边界条件、异常路径和协作行为。
 */
class ChatProductDomainTest {
    @Test
    void groupPlannerStopsAtTurnBudget() {
        GroupChat group =
                new GroupChat(
                        "g",
                        "demo",
                        List.of(new GroupChat.Participant("a", "A", null)),
                        SpeakerPolicy.ROUND_ROBIN,
                        1,
                        100);
        assertTrue(GroupTurnPlanner.next(group, List.of("a"), null, 0).exhausted());
    }

    @Test
    void lorebookEntryIsTruncatedToItsBudget() {
        PromptAssemblyService service = new PromptAssemblyService();
        LorebookEntry entry =
                new LorebookEntry("l", List.of("hello"), "x".repeat(100), 1, "SYSTEM", 5, true);
        var result =
                service.assemble(
                        List.of(),
                        List.of(),
                        null,
                        null,
                        List.of(entry),
                        List.of(),
                        List.of(),
                        null,
                        "hello",
                        20);
        assertTrue(
                result.getFirst().contentParts().getFirst().text().length()
                        <= "lorebook\n".length() + 20);
    }
}
