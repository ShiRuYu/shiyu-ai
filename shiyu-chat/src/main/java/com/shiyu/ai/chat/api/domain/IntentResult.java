package com.shiyu.ai.chat.api.domain;

import lombok.Data;

import java.util.Map;

/**
 * 表示意图识别结果的类
 * 包含意图名称、槽位信息和置信度
 */
@Data
public class IntentResult {

    // 用户意图的名称，表示用户想要执行的操作或表达的目的
    public String intent;
    // 一个键值对映射，包含意图中的关键信息及其对应的值
    // 例如，在"订机票"意图中，可能包含出发地、目的地等槽位信息
    public Map<String, String> slots;
    // 表示意图识别的置信度，取值范围通常在0到1之间
    // 数值越高表示系统对识别结果的信心越强
    public double confidence;
}
