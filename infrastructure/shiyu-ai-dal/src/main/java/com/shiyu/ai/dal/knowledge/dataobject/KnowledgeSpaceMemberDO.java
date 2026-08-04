package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_space_member")
@AutoMapper(target = KnowledgeSpaceMemberBO.class, reverseConvertGenerate = true)
public class KnowledgeSpaceMemberDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long spaceId;
    private String principalType;
    private Long principalId;
    private String spaceRole;
}
