package com.shiyu.ai.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.model.bo.ConversationMessageBO;

@AutoMapper(target = ConversationMessageBO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Table("conversation_message")
public class ConversationMessageDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String sessionId;

    private Long userId;

    private String agentId;

    private String role;

    private String content;

}
