package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.thought.CandidateThought;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@LiteflowComponent("CHAT_TOT")
class ChatToTCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        
        // 获取记忆上下文
        Object memoryContextObj = context.get(GlobalContext.ChatBizKeyEnum.MEMORY_CONTEXT.getCode());

        log.info("开始执行 ToT（Tree of Thoughts）思维树推理：{}", query);

        // Step 1: 生成多个候选方案（思维树分支）
        List<CandidateThought> candidates = generateCandidateThoughts(query, 5, memoryContextObj);
        
        log.info("生成了 {} 个候选方案", candidates.size());

        // Step 2: 评估每个方案并打分
        evaluateAndScoreCandidates(query, candidates);

        // Step 3: 选择最优方案
        CandidateThought bestThought = selectBestThought(candidates);

        if (bestThought != null) {
            log.info("选择最优方案：得分 {}", bestThought.getScore());
            
            // Step 4: 基于最优方案生成最终答案
            String finalAnswer = refineBestAnswer(query, bestThought.getThought());
            
            context.set(GlobalContext.ChatBizKeyEnum.TOT_THOUGHT_NODES.getCode(), 
                    candidates.stream().map(CandidateThought::getThought).toList());
            context.set(GlobalContext.ChatBizKeyEnum.TOT_FINAL_THOUGHT.getCode(), bestThought.getThought());
            context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), finalAnswer);
        } else {
            log.warn("未能选择到有效方案，使用默认回答");
            context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), "抱歉，我暂时无法解答这个问题。");
        }
        
        log.info("ToT 推理完成");
    }

    /**
     * 生成多个候选思维（发散阶段）
     */
    private List<CandidateThought> generateCandidateThoughts(String query, int count, Object memoryContextObj) {
        List<CandidateThought> thoughts = new ArrayList<>();
        
        // 构建带记忆的提示词
        StringBuilder prompt = new StringBuilder();
        
        // 添加记忆上下文
        if (memoryContextObj instanceof com.shiyu.ai.chat.domain.memory.MemoryContext) {
            com.shiyu.ai.chat.domain.memory.MemoryContext memoryContext = 
                (com.shiyu.ai.chat.domain.memory.MemoryContext) memoryContextObj;
            
            if (memoryContext.getMemorySummary() != null && !memoryContext.getMemorySummary().isEmpty()) {
                prompt.append("【相关记忆】\n").append(memoryContext.getMemorySummary()).append("\n\n");
            }
        }
        
        prompt.append("请从不同角度思考以下问题，并提供 ").append(count).append(" 种不同的解决方案或思路。\n\n");
        prompt.append("【问题】").append(query).append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 每个方案应该有独特的视角或方法\n");
        prompt.append("2. 方案之间尽量差异化\n");
        prompt.append("3. 用'方案 1:'、'方案 2:'等格式标注每个方案\n");
        
        String result = chatEngine.call(new ModelRequest(prompt.toString(), PlatformEnum.SILICON_FLOW.getAdapterName(), null));
        
        // 解析返回结果，提取各个方案
        String[] parts = result.split("(?=方案\\d+:)");
        for (String part : parts) {
            if (part.trim().startsWith("方案")) {
                CandidateThought thought = new CandidateThought();
                thought.setThought(part.trim());
                thought.setScore(0.0); // 初始分数为 0
                thoughts.add(thought);
            }
        }
        
        // 如果没有成功解析，至少创建一个默认方案
        if (thoughts.isEmpty()) {
            CandidateThought defaultThought = new CandidateThought();
            defaultThought.setThought(result);
            defaultThought.setScore(0.0);
            thoughts.add(defaultThought);
        }
        
        return thoughts;
    }

    /**
     * 评估并给每个候选方案打分
     */
    private void evaluateAndScoreCandidates(String query, List<CandidateThought> candidates) {
        for (CandidateThought candidate : candidates) {
            try {
                // 构建评估提示词
                String evalPrompt = String.format(
                        "请对以下解决方案进行评分（0-1 分），考虑其：可行性、有效性、创新性。\n\n" +
                                "【原问题】%s\n\n【方案】%s\n\n" +
                                "请只返回一个 0 到 1 之间的数字作为分数，保留两位小数。",
                        query, candidate.getThought());
                
                String scoreResult = chatEngine.call(new ModelRequest(evalPrompt, PlatformEnum.SILICON_FLOW.getAdapterName(), null));
                
                // 解析分数
                double score = parseScore(scoreResult);
                candidate.setScore(score);
                
                log.debug("方案评分：{} -> {}", score, candidate.getThought().substring(0, Math.min(50, candidate.getThought().length())));
                
            } catch (Exception e) {
                log.error("评估方案失败：{}", e.getMessage());
                candidate.setScore(0.5); // 默认中等分数
            }
        }
    }

    /**
     * 解析评分结果
     */
    private double parseScore(String scoreResult) {
        if (scoreResult == null || scoreResult.trim().isEmpty()) {
            return 0.5;
        }
        
        try {
            // 提取数字
            String numberStr = scoreResult.trim().replaceAll("[^0-9.]+", "");
            double score = Double.parseDouble(numberStr);
            
            // 确保在 0-1 范围内
            if (score > 1.0) {
                score = score / 10.0; // 假设是 10 分制
            }
            
            return Math.max(0.0, Math.min(1.0, score));
        } catch (Exception e) {
            log.warn("解析分数失败，使用默认值：{}", scoreResult);
            return 0.5;
        }
    }

    /**
     * 选择最优方案
     */
    private CandidateThought selectBestThought(List<CandidateThought> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        return candidates.stream()
                .max(Comparator.comparingDouble(CandidateThought::getScore))
                .orElse(null);
    }

    /**
     * 基于最优方案精细化生成最终答案
     */
    private String refineBestAnswer(String query, String bestThought) {
        String refinePrompt = String.format(
                "基于以下思考方案，给出完整、清晰的最终答案。\n\n" +
                        "【问题】%s\n\n【最佳方案】%s\n\n" +
                        "请整合以上思路，给出结构清晰、逻辑严谨的最终答案。",
                query, bestThought);
        
        return chatEngine.call(new ModelRequest(refinePrompt, PlatformEnum.SILICON_FLOW.getAdapterName(), null));
    }
}
