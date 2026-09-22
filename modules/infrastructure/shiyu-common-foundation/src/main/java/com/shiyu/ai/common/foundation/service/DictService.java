package com.shiyu.ai.common.foundation.service;

/**
 * 提供 Dict 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface DictService {

    /** 分隔符 */
    String SEPARATOR = ",";

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    default String getDictLabel(String dictType, String dictValue) {
        return getDictLabel(dictType, dictValue, SEPARATOR);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType 字典类型
     * @param dictLabel 字典标签
     * @return 字典值
     */
    default String getDictValue(String dictType, String dictLabel) {
        return getDictValue(dictType, dictLabel, SEPARATOR);
    }

    /** 根据字典类型和值及分隔符获取字典标签。 */
    String getDictLabel(String dictType, String dictValue, String separator);

    /** 根据字典类型和标签及分隔符获取字典值。 */
    String getDictValue(String dictType, String dictLabel, String separator);
}
