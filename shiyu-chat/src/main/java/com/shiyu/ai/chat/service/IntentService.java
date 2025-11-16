package com.shiyu.ai.chat.service;

import com.shiyu.ai.chat.domain.node.Intent;

import java.util.List;

/**
 * IntentService 接口
 * 该接口定义了一个用于检测意图的服务方法
 */
public interface IntentService {
    /**
     * 分类下的意图列表
     */
    List<Intent> list(String category);


    /**
     * 检测输入文本最匹配的意图
     *
     * @param text    用户输入的文本内容
     * @param intents 预定义的意图列表
     * @return 返回最匹配的意图对象，如果没有匹配则返回null
     */
    Intent detect(String text, List<Intent> intents);
}
