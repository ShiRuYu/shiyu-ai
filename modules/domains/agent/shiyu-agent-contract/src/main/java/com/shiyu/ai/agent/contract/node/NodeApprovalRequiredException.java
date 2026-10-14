package com.shiyu.ai.agent.contract.node;

/** Signals that a node has requested approval before performing its side effect. */
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
