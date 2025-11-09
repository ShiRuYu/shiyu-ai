package com.shiyu.ai.chat.api.service;


import com.shiyu.ai.chat.core.model.LMRequest;
import com.shiyu.ai.chat.core.model.LMResponse;
import com.shiyu.ai.chat.core.model.ModelAdapter;
import org.springframework.stereotype.Service;

@Service
public class ChatEngine {

    private final ModelAdapter modelAdapter;

    // 可以注入具体实现，或者通过配置切换 OpenAI/Local
    public ChatEngine(ModelAdapter modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    public String generateResponse(String input) {
        LMRequest request = new LMRequest(input);
        LMResponse response = modelAdapter.generate(request);
        return response.getAnswer();
    }
}

