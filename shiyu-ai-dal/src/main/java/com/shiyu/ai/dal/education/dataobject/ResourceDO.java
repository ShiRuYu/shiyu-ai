package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_resource")
public class ResourceDO extends TenantEntity {

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
    private Long viewCount;
    private LocalDateTime createdAt;
}
