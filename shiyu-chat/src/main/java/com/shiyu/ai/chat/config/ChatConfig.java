package com.shiyu.ai.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@Slf4j
public class ChatConfig {

    /**
     * 提供基于内存的向量存储（SimpleVectorStore）
     * <p>
     * 依赖 EmbeddingModel
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore classificationVectorStore(@Qualifier("siliconFlowEmbeddingModel") EmbeddingModel embeddingModel) {
        return SimpleVectorStore
                .builder(embeddingModel).build();
    }

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(
            @Value("${rag.source:classpath:rag/rag_friendly_classification.txt}") Resource termsOfServiceDocs,
            @Qualifier("classificationVectorStore") VectorStore vectorStore
    ) {

        return args -> {
            // Ingest the document into the vector store
            /*
             * 1、文档读取TextReader 读取 resources/rag/terms-of-service.txt 文件内容
             * 2、TokenTextSplitter 按token长度切分文本（避免大文本超出模型限制）
             * 3、向量化存储 通过 VectorStore.write() 将文本向量存入内存（后续可用于RAG检索）
             */
            vectorStore.write(new TokenTextSplitter().transform(new TextReader(termsOfServiceDocs).read()));

            // 相似性搜索检测
            vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
                log.info("Similar Document: {}", doc.getText());
            });
        };
    }

    /**
     * 存储多轮对话历史（基于内存）
     * 实现上下文感知的连续对话
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }
}
