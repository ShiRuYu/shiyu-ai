package com.shiyu.ai.dal.record.bo;

import com.shiyu.ai.common.core.validate.AddGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.record.dataobject.MediaDO;

/**
 * 附件业务对象
 */
@AutoMapper(target = MediaDO.class, reverseConvertGenerate = true)
@Data
public class MediaBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 记录ID
     */
    @NotNull(message = "记录ID不能为空", groups = { AddGroup.class })
    private Long recordId;

    /**
     * 文件URL
     */
    @NotBlank(message = "文件URL不能为空", groups = { AddGroup.class })
    private String url;

    /**
     * 类型（image/video/audio/file）
     */
    private String type;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 时长（秒，仅音视频）
     */
    private Integer duration;

    /**
     * 宽度（仅图片视频）
     */
    private Integer width;

    /**
     * 高度（仅图片视频）
     */
    private Integer height;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 对象键
     */
    private String objectKey;
}
