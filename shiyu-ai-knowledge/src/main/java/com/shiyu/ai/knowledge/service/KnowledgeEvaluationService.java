package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import jakarta.validation.constraints.NotBlank;

public interface KnowledgeEvaluationService {

    PageData<CaseView> page(int pageNum, int pageSize, Long spaceId);

    CaseView create(CreateCaseRequest request);

    void delete(Long id);

    RunResult run(RunRequest request);

    record CaseView(Long id, Long spaceId, String question,
                    String expectedDocIds, String expectedAnswer) {
    }

    record CreateCaseRequest(Long spaceId, @NotBlank String question,
                             String expectedDocIds, String expectedAnswer) {
    }

    record RunRequest(Long spaceId, Integer topK) {
    }

    record RunResult(Long spaceId, int caseCount, int topK,
                    double recallAtK, double mrr, double citationAccuracy,
                    java.util.List<CaseResult> cases) {
    }

    record CaseResult(Long caseId, String question, double recallAtK,
                      double reciprocalRank, double citationAccuracy,
                      java.util.List<Long> expectedDocumentIds,
                      java.util.List<Long> returnedDocumentIds) {
    }
}
