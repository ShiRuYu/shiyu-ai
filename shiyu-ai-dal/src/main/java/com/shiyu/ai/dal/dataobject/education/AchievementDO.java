package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Table("edu_achievement")
@EqualsAndHashCode(callSuper = true)
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
