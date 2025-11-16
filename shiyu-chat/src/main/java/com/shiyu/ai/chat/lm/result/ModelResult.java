package com.shiyu.ai.chat.lm.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelResult {
    public String answer;
    public double score; // optional confidence
    public List<String> traces; // optional chain traces

    public ModelResult(String answer) {
        this.answer = answer;
    }
}