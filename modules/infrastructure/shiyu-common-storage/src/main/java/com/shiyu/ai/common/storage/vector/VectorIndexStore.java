package com.shiyu.ai.common.storage.vector;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;

import java.util.List;

/**
 * 管理 向量 索引 相关的运行时状态、注册信息或临时数据。
 */
public interface VectorIndexStore {
    /**
     * 执行 向量 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param id 用于定位目标业务对象的标识。
     * @param vector 用于完成本次业务处理的 vector 参数。
     */
    void upsert(String namespace, String id, float[] vector);

    /**
     * 查询 向量 索引 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param vector 用于完成本次业务处理的 vector 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Match> search(String namespace, float[] vector, int limit);

    /**
     * 删除或移除 向量 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param id 用于定位目标业务对象的标识。
     */
    void delete(String namespace, String id);

    /**
     * 封装 Match 相关的不可变数据及其字段约束。
     */
    record Match(String id, float score) {}
}
