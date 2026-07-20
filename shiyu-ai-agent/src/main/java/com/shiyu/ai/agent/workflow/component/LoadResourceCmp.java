package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.dal.education.bo.ResourceBO;
import com.shiyu.ai.dal.education.repository.ResourceRepository;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 加载学习资源组件
 *
 * 为当前知识点加载关联的学习资源（讲义、视频、习题等）。
 */
@Slf4j
@Component("loadResourceCmp")
@RequiredArgsConstructor
public class LoadResourceCmp extends NodeComponent {

    private final ResourceRepository resourceRepository;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("LoadResourceCmp: 加载学习资源, knowledgeId={}", ctx.getKnowledgeId());

        // 加载所有可用资源
        List<ResourceBO> allResources = resourceRepository.selectAll();
        ctx.setResources(allResources);

        log.info("LoadResourceCmp: 加载到 {} 个资源", allResources.size());
    }
}
