package com.shiyu.ai.common.foundation.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;

import com.shiyu.ai.common.foundation.utils.reflect.ReflectUtils;

import java.util.List;

/**
 * 提供 Tree Build 相关的通用辅助操作，供业务和基础设施复用。
 */
public class TreeBuildUtils extends TreeUtil {

    /** 根据前端定制差异化字段 */
    public static final TreeNodeConfig DEFAULT_CONFIG =
            TreeNodeConfig.DEFAULT_CONFIG.setNameKey("label");

    /**
     * 构建或转换 Tree Build 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @param nodeParser 用于完成本次业务处理的 nodeParser 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public static <T, K> List<Tree<K>> build(List<T> list, NodeParser<T, K> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        K k = ReflectUtils.invokeGetter(list.get(0), "parentId");
        return TreeUtil.build(list, k, DEFAULT_CONFIG, nodeParser);
    }
}
