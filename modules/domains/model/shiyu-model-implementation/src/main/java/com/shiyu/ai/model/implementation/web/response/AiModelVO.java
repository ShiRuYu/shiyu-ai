package com.shiyu.ai.model.implementation.web.response;

import com.shiyu.ai.model.implementation.domain.model.AiModelBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@code AiModelVO} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@AutoMappers({@AutoMapper(target = AiModelBO.class), @AutoMapper(target = AiModelResponse.class)})
public class AiModelVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * platformId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long platformId;
    /**
     * modelName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelName;
    /**
     * displayName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String displayName;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * modelConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelConfig;
    /**
     * platformName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String platformName;
    /**
     * isDefault 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isDefault;
    /**
     * sort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sort;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}
