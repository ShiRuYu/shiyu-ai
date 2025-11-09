package com.shiyu.ai.chat.solver.entity;

import lombok.Data;
@Data
public class ThoughtNode {
    private String content;
    private double score;
    public ThoughtNode(String content,double score){this.content=content;this.score=score;}
}
