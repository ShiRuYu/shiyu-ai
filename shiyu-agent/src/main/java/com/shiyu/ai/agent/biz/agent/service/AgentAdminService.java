package com.shiyu.ai.agent.biz.agent.service;

import com.shiyu.ai.model.request.AgentRequest;
import com.shiyu.ai.model.vo.AgentDetailVO;
import com.shiyu.ai.model.vo.AgentVO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import com.shiyu.ai.model.vo.NodeTypeMetaVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AgentAdminService {

    Pair<Long, List<AgentVO>> getPage(Number pageNo, Number pageSize, String name, String status);

    AgentDetailVO getById(Long id);

    AgentVO create(AgentRequest request);

    AgentVO update(Long id, AgentRequest request);

    void deleteById(Long id);

    List<NodeTypeMetaVO> getNodeTypes();

    List<IdNameOptionVO> listAllOptions();
}
