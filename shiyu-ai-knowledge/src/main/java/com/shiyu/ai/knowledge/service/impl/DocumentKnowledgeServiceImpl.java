package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO;
import com.shiyu.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.yomahub.roguemap.memory.MemoryResult;
import com.yomahub.roguemap.memory.RogueMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DocumentKnowledgeServiceImpl implements DocumentKnowledgeService {

    private static final String NS_DOCUMENT = "document";

    private final KnowledgeDocumentRepository documentRepository;
    private final RogueMemory knowledgeRogueMemory;

    public DocumentKnowledgeServiceImpl(KnowledgeDocumentRepository documentRepository,
                                        @Qualifier("knowledgeKeywordMemory") RogueMemory knowledgeRogueMemory) {
        this.documentRepository = documentRepository;
        this.knowledgeRogueMemory = knowledgeRogueMemory;
    }

    @Override
    public KnowledgeDocumentVO getById(Long id) {
        var doc = documentRepository.selectById(id);
        if (doc == null) return null;
        return toVO(doc);
    }

    @Override
    public List<KnowledgeDocumentVO> search(String keyword, int topK) {
        var opts = com.yomahub.roguemap.memory.SearchOptions.builder()
                .namespace(NS_DOCUMENT).build();
        var results = knowledgeRogueMemory.search(keyword, topK, opts);
        List<KnowledgeDocumentVO> list = new ArrayList<>();
        for (MemoryResult r : results) {
            var meta = r.getMetadata();
            if (meta == null) continue;
            try {
                Long id = Long.parseLong(meta.getOrDefault("id", "0"));
                var vo = new KnowledgeDocumentVO(
                        id, meta.getOrDefault("title", ""), r.getContent(),
                        meta.getOrDefault("docType", "ARTICLE"),
                        meta.getOrDefault("source", ""),
                        parseKnowledgeIds(meta.getOrDefault("knowledgeIds", "")));
                list.add(vo);
            } catch (Exception ignored) {}
        }
        return list;
    }

    @Override
    public List<KnowledgeDocumentVO> searchByKnowledgeId(Long knowledgeId) {
        // 通过 RogueMemory 元数据过滤搜索关联文档
        var opts = com.yomahub.roguemap.memory.SearchOptions.builder()
                .namespace(NS_DOCUMENT)
                .filter("knowledgeId", String.valueOf(knowledgeId))
                .build();
        var results = knowledgeRogueMemory.search("", 50, opts);
        List<KnowledgeDocumentVO> list = new ArrayList<>();
        for (MemoryResult r : results) {
            var meta = r.getMetadata();
            if (meta == null) continue;
            try {
                Long id = Long.parseLong(meta.getOrDefault("id", "0"));
                list.add(new KnowledgeDocumentVO(
                        id, meta.getOrDefault("title", ""), r.getContent(),
                        meta.getOrDefault("docType", "ARTICLE"),
                        meta.getOrDefault("source", ""),
                        parseKnowledgeIds(meta.getOrDefault("knowledgeIds", ""))));
            } catch (Exception ignored) {}
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentVO create(CreateDocumentRequest request) {
        var doc = new KnowledgeDocumentDO();
        doc.setTitle(request.title());
        doc.setContent(request.content());
        doc.setDocType(request.docType() != null ? request.docType() : "ARTICLE");
        doc.setSource(request.source());
        doc.setCreateTime(LocalDateTime.now());
        documentRepository.insert(doc);

        // 同步到 RogueMemory
        indexDocument(doc, request.knowledgeIds());

        return toVO(doc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateDocumentRequest request) {
        var doc = documentRepository.selectById(id);
        if (doc == null) return;
        if (request.title() != null) doc.setTitle(request.title());
        if (request.content() != null) doc.setContent(request.content());
        if (request.docType() != null) doc.setDocType(request.docType());
        if (request.source() != null) doc.setSource(request.source());
        doc.setUpdateTime(LocalDateTime.now());
        documentRepository.update(doc);

        // 更新 RogueMemory 索引
        knowledgeRogueMemory.delete(id.toString());
        indexDocument(doc, request.knowledgeIds());
    }

    @Override
    public void delete(Long id) {
        documentRepository.deleteById(id);
        knowledgeRogueMemory.delete(id.toString());
    }

    private void indexDocument(KnowledgeDocumentDO doc, List<Long> knowledgeIds) {
        String id = String.valueOf(doc.getId());
        String content = doc.getTitle() + " " + (doc.getContent() != null ? doc.getContent() : "");

        Map<String, String> meta = new HashMap<>();
        meta.put("id", id);
        meta.put("title", doc.getTitle());
        meta.put("docType", doc.getDocType());
        meta.put("source", doc.getSource() != null ? doc.getSource() : "");
        if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
            meta.put("knowledgeId", String.valueOf(knowledgeIds.get(0))); // 主关联
            meta.put("knowledgeIds", String.join(",", knowledgeIds.stream().map(String::valueOf).toList()));
        }
        knowledgeRogueMemory.add(content, meta, NS_DOCUMENT);
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocumentDO doc) {
        return new KnowledgeDocumentVO(
                doc.getId(), doc.getTitle(), doc.getContent(), doc.getDocType(),
                doc.getSource(), List.of());
    }

    private List<Long> parseKnowledgeIds(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(",")).map(Long::parseLong).toList();
    }
}
