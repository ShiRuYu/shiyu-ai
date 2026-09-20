package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

/**
 * 定义 工具 Approval 可用的枚举值及其业务语义。
 */
public enum ToolApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}
