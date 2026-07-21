package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.KnowledgeTextbookDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * KnowledgeTextbook 业务对象
 */
@AutoMapper(target = KnowledgeTextbookDO.class, reverseConvertGenerate = true)
@Data
public class KnowledgeTextbookBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long knowledgeId;

    private Long textbookId;

    private Long chapterId;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
