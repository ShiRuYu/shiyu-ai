package com.shiyu.ai.knowledge.config;

import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(3)
public class IndexInitializer implements ApplicationRunner {

    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;
    private final VectorStore vectorStore;

    public IndexInitializer(KnowledgeSearchService knowledgeSearchService,
                            KnowledgeDocumentRepository documentRepository,
                            DocumentIngestionService ingestionService,
                            VectorStore vectorStore) {
        this.knowledgeSearchService = knowledgeSearchService;
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        // HnswVectorStore 启动时已自动从磁盘加载索引，
        // 如果已有数据则跳过重建，避免"加载→清空→重建"的无用功。
        // 如需手动同步 DB 最新数据，可调用 POST /knowledge/index/rebuild
        int size = vectorStore.size();

        if (size > 0) {
            log.info("向量索引已有 {} 条记录（已从磁盘加载），跳过重建", size);
            return;
        }

        log.info("向量索引为空，开始从 DB 重建...");
        try {
            knowledgeSearchService.rebuildIndex();
            log.info("知识点向量索引重建完成");
        } catch (Exception e) {
            log.error("知识点向量索引重建失败", e);
        }

        log.info("开始重建文档向量索引...");
        try {
            var docs = documentRepository.selectAll();
            int count = 0;
            for (var doc : docs) {
                String content = doc.getContent() != null ? doc.getContent() : "";
                ingestionService.ingest(doc.getId(), content, List.of());
                count++;
            }
            log.info("文档向量索引重建完成: {} 篇文档", count);
        } catch (Exception e) {
            log.error("文档向量索引重建失败", e);
        }

        log.info("向量索引初始化完成");
    }
}
