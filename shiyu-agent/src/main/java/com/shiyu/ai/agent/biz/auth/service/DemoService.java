package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.vo.MenuAllVO;
import com.shiyu.ai.agent.domain.vo.ProductVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.Date;
import java.util.List;

/**
 * 演示服务接口
 */
public interface DemoService {

    /**
     * 获取所有菜单列表（模拟数据）
     *
     * @return 菜单列表
     */
    List<MenuAllVO> getAllMenus();

    /**
     * 获取表格数据列表（模拟数据）
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param category 分类过滤
     * @param start 开始日期
     * @param end 结束日期
     * @return 分页结果
     */
    PageData<ProductVO> getTableList(Integer page, Integer pageSize, String category, Date start, Date end);
}
