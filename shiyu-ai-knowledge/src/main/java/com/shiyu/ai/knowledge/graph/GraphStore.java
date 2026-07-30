package com.shiyu.ai.knowledge.graph;



import com.shiyu.ai.knowledge.domain.GraphEdge;

import com.shiyu.ai.knowledge.domain.GraphNode;



import java.util.List;

import java.util.Set;



public interface GraphStore {



    GraphNode getNode(Long id);



    List<Long> parents(Long id);



    List<Long> children(Long id);



    List<Long> related(Long id);



    List<GraphEdge> edges(Long id);



    void addNode(GraphNode node);



    void addEdge(Long sourceId, Long targetId, String type, double weight);



    void removeEdge(Long sourceId, Long targetId, String type);






    /**
     * 移除节点及其所有关联边
     */
    void removeNode(Long id);

/** 获取父节点列表 (返回完整节点) */

    List<GraphNode> getParentNodes(Long id);



    /** 获取子节点列表 (返回完整节点) */

    List<GraphNode> getChildNodes(Long id);



    /** 获取相关节点列表 (返回完整节点) */

    List<GraphNode> getRelatedNodes(Long id);

    List<Long> topologicalSort(Long rootId);



    List<Long> dfs(Long startId);



    List<Long> bfs(Long startId);



    List<Long> findPath(Long from, Long to);



    List<Long> findMissingPrerequisites(Long targetId, Set<Long> masteredIds);



    void loadAll();

}
