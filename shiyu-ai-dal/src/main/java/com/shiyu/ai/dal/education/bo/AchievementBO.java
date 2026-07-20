package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.AchievementDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Achievement 业务对象
 */
@AutoMapper(target = AchievementDO.class, reverseConvertGenerate = true)
@Data
public class AchievementBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private String code;

    private String name;

    private String description;

    private String icon;

    private LocalDateTime earnedAt;

}
