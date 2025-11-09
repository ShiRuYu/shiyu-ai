package com.shiyu.ai.chat.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LMRequest {
    public String prompt;
    public Map<String,Object> meta; // temperature, maxTokens, modelName...
    public List<Message> messages; // optional for chat models

    public LMRequest(String prompt) {
        this.prompt = prompt;
    }

    public LMRequest(String prompt, Map<String,Object> temperature) {
        this.prompt = prompt;
        this.meta = temperature;
    }
}
