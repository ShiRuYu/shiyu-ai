package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.ChapterDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Chapter 业务对象
 */
@AutoMapper(target = ChapterDO.class, reverseConvertGenerate = true)
@Data
public class ChapterBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long textbookId;

    private Long parentId;

    private String name;

    private Integer chapterOrder;

}
