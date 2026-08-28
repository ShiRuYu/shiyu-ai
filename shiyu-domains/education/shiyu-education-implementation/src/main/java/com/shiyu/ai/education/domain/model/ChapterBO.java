package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Chapter 业务对象
 */
@Data
public class ChapterBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long textbookId;

    private Long parentId;

    private String name;

    private Integer chapterOrder;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
