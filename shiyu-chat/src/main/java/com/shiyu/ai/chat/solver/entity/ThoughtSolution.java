package com.shiyu.ai.chat.solver.entity;

import lombok.Data;
import java.util.List;
@Data
public class ThoughtSolution {
    private List<ThoughtNode> nodes;
    private ThoughtNode bestNode;
}