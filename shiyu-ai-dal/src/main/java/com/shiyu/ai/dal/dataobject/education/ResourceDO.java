package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("resource")
public class ResourceDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
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
    private Integer status;
    private Long viewCount;
    private LocalDateTime createdAt;
}
