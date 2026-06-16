package com.shiyu.ai.agent.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table(value = "long_term_memory")
public class LongTermMemoryDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String agentId;

    private Long tenantId;

    private String category;

    private String memoryKey;

    private String content;

    private Double importance;

    private String source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
