package com.shiyu.ai.chat.core.intent;

public class IntentService {
    public String detectIntent(String input) {
// 简单模拟
        if(input.contains("规划")) return "planning_task";
        return "general_task";
    }
}
