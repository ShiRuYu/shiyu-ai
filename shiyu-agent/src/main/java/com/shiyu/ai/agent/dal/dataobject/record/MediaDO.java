package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 附件数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "media")
public class MediaDO extends BaseEntity {

    /**
     * 附件ID
     */
    @Id(keyType = KeyType.Auto)
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
     * 存储桶
     */
    private String bucket;

    /**
     * 对象键
     */
    private String objectKey;
}
