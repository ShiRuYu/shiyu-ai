package com.shiyu.ai.record.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.io.Serial;
import com.shiyu.ai.record.domain.model.ProfileBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 人物数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "record_profile")
@AutoMapper(target = ProfileBO.class, reverseConvertGenerate = true)
public class ProfileDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 人物ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别（0男 1女 2未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 头像URL
     */
    private String avatar;

}
