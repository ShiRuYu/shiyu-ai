package com.shiyu.ai.agent.contract.node;

/** 表示节点在执行有副作用的操作前请求审批。 */
public final class NodeApprovalRequiredException extends RuntimeException {
    @java.io.Serial private static final long serialVersionUID = 1L;

    private final String approvalId;

    public NodeApprovalRequiredException(String approvalId) {
        super("tool approval is required");
        if (approvalId == null || approvalId.isBlank()) {
            throw new IllegalArgumentException("approvalId is required");
        }
        this.approvalId = approvalId;
    }

    public String approvalId() {
        return approvalId;
    }
}
