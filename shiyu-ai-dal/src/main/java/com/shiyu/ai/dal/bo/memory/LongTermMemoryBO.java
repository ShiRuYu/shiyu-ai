package com.shiyu.ai.dal.bo.memory;

import lombok.Data;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.dataobject.agent.LongTermMemoryDO;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 长期记忆业务对象
 */
@AutoMapper(target = LongTermMemoryDO.class, reverseConvertGenerate = true)
@Data
public class LongTermMemoryBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记忆 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 分类（general/preference/habit/fact 等）
     */
    private String category;

    /**
     * 记忆键
     */
    private String memoryKey;

    /**
     * 记忆内容
     */
    private String content;

    /**
     * 重要性分数
     */
    private Double importance;

    /**
     * 来源
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
