package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeEvaluationCaseDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.KnowledgeEvaluationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KnowledgeEvaluationServiceImpl implements KnowledgeEvaluationService {

    private final KnowledgeEnterpriseRepository repository;
    private final KnowledgeSpaceService spaceService;
    private final KnowledgeIndexService indexService;

    @Override
    public PageData<CaseView> page(int pageNum, int pageSize, Long spaceId) {
        if (spaceId == null) throw new ServiceException("必须指定知识空间");
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
        PageData<KnowledgeEvaluationCaseDO> page = repository.pageEvaluations(pageNum, Math.min(pageSize, 100), spaceId);
        return new PageData<>(page.getItems().stream().map(this::toView).toList(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CaseView create(CreateCaseRequest request) {
        if (request.spaceId() == null) throw new ServiceException("必须指定知识空间");
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        KnowledgeEvaluationCaseDO evaluation = new KnowledgeEvaluationCaseDO();
        evaluation.setTenantId(LoginContextHolder.getCurrentTenantId());
        evaluation.setSpaceId(request.spaceId());
        evaluation.setQuestion(request.question().trim());
        evaluation.setExpectedDocIds(request.expectedDocIds());
        evaluation.setExpectedAnswer(request.expectedAnswer());
        evaluation.setStatus(1);
        evaluation.setDelFlag(0);
        repository.insertEvaluation(evaluation);
        return toView(evaluation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgeEvaluationCaseDO evaluation = repository.findEvaluation(id);
        if (evaluation == null) throw new ServiceException("评测用例不存在: " + id);
        spaceService.requireAccess(evaluation.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        repository.deleteEvaluation(id);
    }

    @Override
    public RunResult run(RunRequest request) {
        if (request == null || request.spaceId() == null) {
            throw new ServiceException("必须指定知识空间");
        }
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        int topK = request.topK() == null ? 5 : Math.max(1, Math.min(request.topK(), 100));
        PageData<KnowledgeEvaluationCaseDO> page = repository.pageEvaluations(1, 1000, request.spaceId());
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        if (tenantId == null) throw new ServiceException("当前租户上下文不存在");
        List<CaseResult> results = page.getItems().stream()
                .map(value -> evaluateCase(value, tenantId, topK))
                .toList();
        double recall = results.stream().mapToDouble(CaseResult::recallAtK).average().orElse(0D);
        double mrr = results.stream().mapToDouble(CaseResult::reciprocalRank).average().orElse(0D);
        double citationAccuracy = results.stream().mapToDouble(CaseResult::citationAccuracy).average().orElse(0D);
        return new RunResult(request.spaceId(), results.size(), topK, recall, mrr, citationAccuracy, results);
    }

    private CaseResult evaluateCase(KnowledgeEvaluationCaseDO value, Long tenantId, int topK) {
        Set<Long> expected = parseDocumentIds(value.getExpectedDocIds());
        List<Long> returned = indexService.hybridSearch(tenantId, value.getSpaceId(), value.getQuestion(), topK, true)
                .stream().map(KnowledgeIndexService.HybridHit::documentId).filter(java.util.Objects::nonNull).distinct().toList();
        long relevant = returned.stream().filter(expected::contains).count();
        double recall = expected.isEmpty() ? 0D : (double) relevant / expected.size();
        double reciprocalRank = 0D;
        for (int i = 0; i < returned.size(); i++) {
            if (expected.contains(returned.get(i))) {
                reciprocalRank = 1D / (i + 1);
                break;
            }
        }
        double citationAccuracy = returned.isEmpty() ? 0D : (double) relevant / returned.size();
        return new CaseResult(value.getId(), value.getQuestion(), recall, reciprocalRank,
                citationAccuracy, List.copyOf(expected), returned);
    }

    private Set<Long> parseDocumentIds(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();
        return Arrays.stream(raw.replace('[', ' ').replace(']', ' ')
                        .replace('"', ' ').split("[,;\\s]+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try { return Long.valueOf(value); }
                    catch (NumberFormatException ignored) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private CaseView toView(KnowledgeEvaluationCaseDO value) {
        return new CaseView(value.getId(), value.getSpaceId(), value.getQuestion(),
                value.getExpectedDocIds(), value.getExpectedAnswer());
    }
}
