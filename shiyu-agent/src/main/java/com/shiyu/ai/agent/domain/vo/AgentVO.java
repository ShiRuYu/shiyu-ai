package com.shiyu.ai.agent.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVO {

    private Long id;

    private String agentId;

    private String name;

    private String description;

    private String currentVersion;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
