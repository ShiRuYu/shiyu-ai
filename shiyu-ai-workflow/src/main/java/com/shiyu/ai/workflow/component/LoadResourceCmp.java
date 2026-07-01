package com.shiyu.ai.workflow.component;

import com.shiyu.ai.dal.dataobject.education.ResourceDO;
import com.shiyu.ai.education.resource.ResourceService;
import com.shiyu.ai.workflow.context.LearningContext;
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

    private final ResourceService resourceService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("LoadResourceCmp: 加载学习资源, knowledgeId={}", ctx.getKnowledgeId());

        // 加载所有可用资源
        List<ResourceDO> allResources = resourceService.listAll();
        ctx.setResources(allResources);

        log.info("LoadResourceCmp: 加载到 {} 个资源", allResources.size());
    }
}
