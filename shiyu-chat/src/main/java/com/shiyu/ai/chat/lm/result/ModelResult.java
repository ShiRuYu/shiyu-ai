package com.shiyu.ai.chat.lm.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelResult {
    public String answer;
    public double score; // optional confidence
    public List<String> traces; // optional chain traces
    public Flux<String> answerStream;

    public ModelResult(String answer) {
        this.answer = answer;
    }
    public ModelResult(Flux<String> answerStream) {
        this.answerStream = answerStream;
    }
}