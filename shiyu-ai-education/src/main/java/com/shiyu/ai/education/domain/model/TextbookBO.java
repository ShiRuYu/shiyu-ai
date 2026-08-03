package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Textbook 业务对象
 */
@Data
public class TextbookBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String subjectCode;

    private Integer grade;

    private String publisher;

    private String isbn;

    private LocalDateTime createTime;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
