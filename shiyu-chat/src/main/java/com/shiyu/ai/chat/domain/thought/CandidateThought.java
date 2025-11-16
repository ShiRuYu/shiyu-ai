package com.shiyu.ai.chat.domain.thought;

import lombok.Data;

@Data
public class CandidateThought {
    private String thought;
    private double score;
}
