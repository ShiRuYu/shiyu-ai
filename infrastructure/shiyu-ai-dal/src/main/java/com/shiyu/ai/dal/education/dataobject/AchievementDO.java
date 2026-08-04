package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.AchievementBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_achievement")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AchievementBO.class, reverseConvertGenerate = true)
public class AchievementDO extends TenantEntity {
    @Serial private static final long serialVersionUID = 1L;
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long studentId;
    private String code;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime earnedAt;
}
