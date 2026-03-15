package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.StreamResult;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 执行流式对话调用
 */
@Slf4j
@LiteflowComponent("STREAM_EXECUTE")
public class StreamExecuteCmp extends NodeComponent {
    
    @Resource
    private ChatEngine chatEngine;
    
    @Override
    public void process() {
        try {
            log.info("执行 STREAM_EXECUTE 节点");
            GlobalContext context = this.getContextBean(GlobalContext.class);
            
            // 获取请求参数
            String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
            String platform = context.get(GlobalContext.ChatBizKeyEnum.PLATFORM.getCode(), "SILICON_FLOW");
            String modelName = context.get(GlobalContext.ChatBizKeyEnum.MODEL_NAME.getCode());
            
            log.info("流式对话请求：query={}, platform={}, modelName={}", query, platform, modelName);
            
            // 构建模型请求
            LmRequest request = new LmRequest(query, platform, modelName);
            
            // 执行流式调用
            StreamResult result = chatEngine.stream(request);
            
            // 将 Flux 存入全局上下文
            Flux<String> flux = result.getAnswer();
            context.set(GlobalContext.ChatBizKeyEnum.STREAM_FLUX.getCode(), flux);
            
            log.info("流式调用执行成功");
            
        } catch (Exception e) {
            log.error("STREAM_EXECUTE 异常：{}", e.getMessage(), e);
            throw new RuntimeException("流式调用失败：" + e.getMessage(), e);
        }
    }
}
