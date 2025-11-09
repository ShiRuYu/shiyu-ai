package com.shiyu.ai.chat.api.service;

import com.shiyu.ai.chat.api.domain.IntentResult;
import com.shiyu.ai.chat.core.context.GlobalContext;

/**
 * IntentService 接口
 * 该接口定义了一个用于检测意图的服务方法
 */
public interface IntentService {
    /**
     * 检测输入文本中的意图
     *
     * @param text 需要检测的文本内容
     * @param gtx 会话上下文，包含当前会话的相关信息
     * @return IntentResult 检测结果，包含识别出的意图信息
     */
    IntentResult detect(String text, GlobalContext gtx);

}
