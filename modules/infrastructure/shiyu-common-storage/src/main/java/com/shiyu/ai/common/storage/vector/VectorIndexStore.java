package com.shiyu.ai.common.storage.vector;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;

import java.util.List;

/**
 * VectorIndexStore 接口，定义基础设施模块的能力边界。
 */
public interface VectorIndexStore {
    /**
     * 保存或更新业务对象。
     *
     * @param namespace 方法参数。
     * @param id 目标对象标识。
     * @param vector 方法参数。
     */
    void upsert(String namespace, String id, float[] vector);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param namespace 方法参数。
     * @param vector 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Match> search(String namespace, float[] vector, int limit);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param namespace 方法参数。
     * @param id 目标对象标识。
     */
    void delete(String namespace, String id);

    /**
     * {@code Match} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param score 分数，表示该记录组件承载的数据。
     */
    record Match(String id, float score) {}
}
