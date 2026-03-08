package com.shiyu.ai.chat.config;

import com.shiyu.ai.chat.domain.node.Intent;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图识别配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.intent")
public class IntentConfig {

    /**
     * 意图分类配置
     * key: 分类名称
     * value: 该分类下的意图列表
     */
    private Map<String, List<IntentDefinition>> categories = new HashMap<>();

    /**
     * 意图定义
     */
    @Data
    public static class IntentDefinition {
        /**
         * 意图 ID
         */
        private String id;

        /**
         * 意图名称
         */
        private String name;

        /**
         * 意图类型：DIRECT、COT、TOT
         */
        private String type;

        /**
         * 意图描述
         */
        private String content;

        /**
         * 关键词列表，用于快速匹配
         */
        private List<String> keywords = new ArrayList<>();

        /**
         * 要调用的子链名称
         */
        private String chainToCall = "chatDirect";

        /**
         * 转换为 Intent 对象
         */
        public Intent toIntent() {
            Intent intent = new Intent();
            intent.setId(this.id);
            intent.setName(this.name);
            intent.setType(this.type);
            intent.setContent(this.content);
            intent.setChainToCall(this.chainToCall);
            return intent;
        }
    }
}
