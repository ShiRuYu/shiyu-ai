package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("chapter")
public class ChapterDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long textbookId;
    private Long parentId;
    private String name;
    private Integer chapterOrder;
    private LocalDateTime createTime;
}
