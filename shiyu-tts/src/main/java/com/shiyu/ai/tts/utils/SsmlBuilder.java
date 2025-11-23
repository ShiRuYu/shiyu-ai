package com.shiyu.ai.tts.utils;

public class SsmlBuilder {
    /**
     * 构建 SSML 请求体
     * @param text 要合成的文本
     * @param voice 语音名称（如 zh-CN-XiaoxiaoNeural）
     * @param rate 语速比例，例如 1.0f 表示 100%
     * @return SSML 字符串
     */
    public static String buildRequest(String text, String voice, float rate) {
        return String.format(
                "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>" +
                        "<voice name='%s'>" +
                        "<prosody rate='%.0f%%'>%s</prosody>" +
                        "</voice>" +
                        "</speak>",
                voice, rate * 100, text
        );
    }
}

