package com.shiyu.ai.chat.api.domain;

import lombok.Data;

@Data
public class CandidateThought {
    private String thought;
    private double score;
}
