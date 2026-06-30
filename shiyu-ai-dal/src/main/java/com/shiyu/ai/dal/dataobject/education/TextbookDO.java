package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("textbook")
public class TextbookDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;
    private String subjectCode;
    private Integer grade;
    private String publisher;
    private String isbn;
    private LocalDateTime createTime;
}
