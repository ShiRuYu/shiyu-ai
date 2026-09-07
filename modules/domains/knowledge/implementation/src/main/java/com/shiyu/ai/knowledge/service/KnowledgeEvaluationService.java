package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import jakarta.validation.constraints.NotBlank;

public interface KnowledgeEvaluationService {

    PageData<CaseView> page(ActorContext actor, int pageNum, int pageSize, Long spaceId);

    CaseView create(ActorContext actor, CreateCaseRequest request);

    void delete(ActorContext actor, Long id);

    RunResult run(ActorContext actor, RunRequest request);

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
