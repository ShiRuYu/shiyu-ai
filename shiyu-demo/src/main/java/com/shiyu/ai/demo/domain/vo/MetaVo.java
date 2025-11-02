package com.shiyu.ai.demo.domain.vo;

import com.shiyu.ai.common.core.utils.StringUtils;
import lombok.Data;

/**
 * 路由显示信息
 */

@Data
public class MetaVo {

    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，被 <keep-alive>缓存
     */
    private boolean cache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean cache) {
        this.title = title;
        this.icon = icon;
        this.cache = cache;
    }

    public MetaVo(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }

    public MetaVo(String title, String icon, boolean cache, String link) {
        this.title = title;
        this.icon = icon;
        this.cache = cache;
        if (StringUtils.isHttp(link)) {
            this.link = link;
        }
    }

}
