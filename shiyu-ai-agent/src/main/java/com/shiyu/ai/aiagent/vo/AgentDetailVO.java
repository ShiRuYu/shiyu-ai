package com.shiyu.ai.aiagent.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailVO {

    private Long id;

    private String agentId;

    private String name;

    private String description;

    private String currentVersion;

    private String status;

    private List<AgentVersionVO> versions;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
