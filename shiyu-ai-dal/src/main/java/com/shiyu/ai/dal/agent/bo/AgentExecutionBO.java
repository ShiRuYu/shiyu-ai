package com.shiyu.ai.dal.agent.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.agent.dataobject.AgentExecutionDO;

/**
 * Agent 执行记录业务对象
 */
@AutoMapper(target = AgentExecutionDO.class, reverseConvertGenerate = true)
@Data
public class AgentExecutionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 执行 ID（唯一标识）
     */
    private String executionId;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 节点 ID
     */
    private String nodeId;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 输入数据（JSON）
     */
    private String inputData;

    /**
     * 输出数据（JSON）
     */
    private String outputData;

    /**
     * 状态（RUNNING/SUCCESS/FAILED）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    private Long durationMs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
