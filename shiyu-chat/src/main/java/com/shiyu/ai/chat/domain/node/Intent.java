package com.shiyu.ai.chat.domain.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class Intent extends Node {
    // 一个键值对映射，包含意图中的关键信息及其对应的值
    // 例如，在"订机票"意图中，可能包含出发地、目的地等槽位信息
    private Map<String, String> slots;
    // 表示意图识别的置信度，取值范围通常在0到1之间
    // 数值越高表示系统对识别结果的信心越强
    private double confidence;
    // 根据选择的策略调用对应子链
    private String chainToCall = "chatDirect";

}
