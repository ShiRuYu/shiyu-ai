package com.shiyu.ai.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 意图类型枚举
 * 定义系统中支持的各种意图类型
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Getter
@AllArgsConstructor
public enum IntentType {

    /**
     * 闲聊意图
     */
    CHITCHAT("CHITCHAT", "闲聊", "处理用户的日常闲聊对话"),

    /**
     * 问答意图
     */
    QUESTION("QUESTION", "问答", "处理用户的知识性问题"),

    /**
     * 计算器
     */
    CALCULATOR("CALCULATOR", "计算器", "执行基础的数学运算（加、减、乘、除）"),

    /**
     * 查询意图
     */
    QUERY("QUERY", "查询", "处理数据或信息查询请求"),

    /**
     * 建议意图
     */
    SUGGESTION("SUGGESTION", "建议", "处理用户提出的建议或反馈"),

    /**
     * 投诉意图
     */
    COMPLAINT("COMPLAINT", "投诉", "处理用户的投诉"),

    /**
     * 技术支持意图
     */
    TECHNICAL_SUPPORT("TECHNICAL_SUPPORT", "技术支持", "处理技术咨询和支持问题"),

    /**
     * 产品咨询意图
     */
    PRODUCT_INQUIRY("PRODUCT_INQUIRY", "产品咨询", "处理产品相关的咨询"),

    /**
     * 订单处理意图
     */
    ORDER_PROCESSING("ORDER_PROCESSING", "订单处理", "处理订单相关的操作"),

    /**
     * 预约意图
     */
    APPOINTMENT("APPOINTMENT", "预约", "处理预约和时间安排"),

    /**
     * 导航意图
     */
    NAVIGATION("NAVIGATION", "导航", "处理路线和位置导航"),

    /**
     * 娱乐意图
     */
    ENTERTAINMENT("ENTERTAINMENT", "娱乐", "处理娱乐相关的内容"),

    /**
     * 教育意图
     */
    EDUCATION("EDUCATION", "教育", "处理教育和学习内容"),

    /**
     * 健康意图
     */
    HEALTH("HEALTH", "健康", "处理健康和医疗相关的咨询"),

    /**
     * 金融意图
     */
    FINANCE("FINANCE", "金融", "处理金融和财务相关的问题"),

    /**
     * 购物意图
     */
    SHOPPING("SHOPPING", "购物", "处理购物相关的请求"),

    /**
     * 旅行意图
     */
    TRAVEL("TRAVEL", "旅行", "处理旅行和旅游相关的规划"),

    /**
     * 天气查询
     */
    WEATHER("WEATHER", "天气查询", "查询指定城市的当前天气信息"),

    /**
     * 新闻意图
     */
    NEWS("NEWS", "新闻", "处理新闻资讯查询"),

    /**
     * 翻译意图
     */
    TRANSLATION("TRANSLATION", "翻译", "处理语言翻译请求"),

    /**
     * 代码帮助意图
     */
    CODE_HELP("CODE_HELP", "代码帮助", "处理编程和技术问题"),

    /**
     * 写作辅助意图
     */
    WRITING_ASSISTANCE("WRITING_ASSISTANCE", "写作辅助", "处理写作和文本生成任务"),

    /**
     * 数据分析意图
     */
    DATA_ANALYSIS("DATA_ANALYSIS", "数据分析", "处理数据分析和解释"),

    /**
     * 未知意图
     */
    UNKNOWN("UNKNOWN", "未知", "无法识别的意图类型");

    /**
     * 意图类型代码
     */
    private final String code;

    /**
     * 意图名称
     */
    private final String name;

    /**
     * 意图描述
     */
    private final String description;

    /**
     * 根据代码获取意图类型
     *
     * @param code 意图类型代码
     * @return 对应的意图类型，如果未找到则返回 UNKNOWN
     */
    public static IntentType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN;
        }

        for (IntentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据名称获取意图类型
     *
     * @param name 意图名称
     * @return 对应的意图类型，如果未找到则返回 UNKNOWN
     */
    public static IntentType fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }

        for (IntentType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
