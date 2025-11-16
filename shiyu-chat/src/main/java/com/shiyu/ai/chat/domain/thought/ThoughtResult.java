package com.shiyu.ai.chat.domain.thought;

import lombok.Data;

import java.util.List;

@Data
public class ThoughtResult {
    public String finalAnswer;
    public List<CandidateThought> candidates;
}
