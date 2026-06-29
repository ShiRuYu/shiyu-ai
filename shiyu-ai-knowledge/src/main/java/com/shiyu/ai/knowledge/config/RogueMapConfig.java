package com.shiyu.ai.knowledge.config;

import com.yomahub.roguemap.RogueMap;
import com.yomahub.roguemap.serialization.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RogueMapConfig {

    @Bean
    public RogueMap<String, String> graphRogueMap() {
        return RogueMap.<String, String>mmap()
                .persistent("./data/knowledge-graph.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .keyCodec(StringCodec.INSTANCE)
                .valueCodec(StringCodec.INSTANCE)
                .build();
    }
}
