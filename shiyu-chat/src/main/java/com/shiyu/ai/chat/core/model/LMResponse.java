package com.shiyu.ai.chat.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LMResponse {
    public String answer;
    public double score; // optional confidence
    public List<String> traces; // optional chain traces

    public LMResponse(String answer) {
        this.answer = answer;
    }
}