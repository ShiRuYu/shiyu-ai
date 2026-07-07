package com.shiyu.ai.aiagent.workflow.context;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TutorContext {
    private Long studentId;
    private Long knowledgeId;
    private String knowledgeName;
    private String teachContent;
    private Double practiceScore;
    private Boolean reviewNeeded;
}
