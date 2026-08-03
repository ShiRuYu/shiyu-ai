package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Achievement 业务对象
 */
@Data
public class AchievementBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private String code;

    private String name;

    private String description;

    private String icon;

    private LocalDateTime earnedAt;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
