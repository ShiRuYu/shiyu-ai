package com.shiyu.ai.common.core.service;

/**
 * 通用 OSS服务
 */
public interface OssService {

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    /**
     * Select Url By Ids
     * @return 处理结果
     */
    String selectUrlByIds(String ossIds);

}
