package com.shiyu.ai.education.agent.node.creator;

import com.shiyu.ai.education.agent.graph.ReviewScheduleNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.dal.education.repository.ReviewTaskRepository;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.service.ReviewService;
import org.springframework.stereotype.Component;

@Component
public class ReviewScheduleNodeCreator implements NodeCreator {

    private final ReviewScheduler reviewScheduler;
    private final ReviewService reviewService;
    private final ReviewTaskRepository reviewTaskRepository;

    public ReviewScheduleNodeCreator(ReviewScheduler reviewScheduler,
                                     ReviewService reviewService,
                                     ReviewTaskRepository reviewTaskRepository) {
        this.reviewScheduler = reviewScheduler;
        this.reviewService = reviewService;
        this.reviewTaskRepository = reviewTaskRepository;
    }

    @Override
    public NodeType getType() {
        return NodeType.REVIEW_SCHEDULE;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        ReviewScheduleNode node = new ReviewScheduleNode(reviewScheduler, reviewService, reviewTaskRepository);
        node.setConfig(config);
        return node;
    }
}
