package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 附件视图对象
 */
@Data
public class MediaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 文件URL
     */
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
