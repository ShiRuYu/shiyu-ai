package com.shiyu.ai.dal.mapper.knowledge;

import com.mybatisflex.core.BaseMapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * Knowledge Document 接口
 */

public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentDO> {
}
