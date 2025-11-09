package com.shiyu.ai.chat.api.domain;

import lombok.Data;

import java.util.List;

@Data
public class StrategyResult {
    public String finalAnswer;
    public List<CandidateThought> candidates;
}
