package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

/**
 * {@code ToolApprovalStatus} 表示智能体模块中的一组受控业务状态或分类。
 */
public enum ToolApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}
