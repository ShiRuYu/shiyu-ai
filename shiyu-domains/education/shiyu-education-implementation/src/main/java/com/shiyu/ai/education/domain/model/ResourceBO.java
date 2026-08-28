package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Resource 业务对象
 */
@Data
public class ResourceBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String type;

    private String url;

    private Long sizeBytes;

    private Integer durationSec;

    private String subjectCode;

    private Integer grade;

    private Integer difficulty;

    private String coverUrl;

    private String description;

    private Long viewCount;

    private LocalDateTime createdAt;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
