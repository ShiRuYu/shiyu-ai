package com.shiyu.ai.auth.domain.vo;

import com.shiyu.ai.common.core.utils.StringUtils;
import lombok.Data;

/**
 * 路由显示信息
 */

@Data
public class MetaVO {

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

    public MetaVO(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVO(String title, String icon, boolean cache) {
        this.title = title;
    // 使用传入的icon参数初始化对象的icon属性
        this.icon = icon;
    // 使用传入的cache参数初始化对象的cache属性
        this.cache = cache;
    }

    public MetaVO(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }

    public MetaVO(String title, String icon, boolean cache, String link) {
        this.title = title;
        this.icon = icon;
        this.cache = cache;
        if (StringUtils.isHttp(link)) {
            this.link = link;
        }
    }

}
