package com.shiyu.ai.common.core.enums;

import com.shiyu.ai.common.core.CharConstants;
import com.shiyu.ai.common.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * todo
 */
@Getter
@AllArgsConstructor
public enum ComponentTypeEnum {
    /**
     * Layout组件标识
     * 主布局容器
     * @/layout/index.vue
     * 一级菜单根节点
     */
    LAYOUT("Layout"),

    /**
     * ParentView组件标识
     * 中间父级占位符
     * @/components/ParentView.vue
     * 二级或三级嵌套菜单
     */
    PARENT_VIEW("ParentView"),

    /**
     * InnerLink组件标识
     * 内嵌外链
     * @/components/InnerLink.vue
     * 打开外部或内嵌页面
     */
    INNER_LINK("InnerLink");

    private final String value;

    /**
     * 根据值获取枚举
     */
    public static ComponentTypeEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(componentTypeEnum -> componentTypeEnum.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    public boolean isLayout() {
        return this == LAYOUT;
    }

    public boolean isParentView() {
        return this == PARENT_VIEW;
    }

    public boolean isInnerLink() {
        return this == INNER_LINK;
    }
    /**
     * 内链域名特殊字符替换
     */
    public static String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[]{CharConstants.HTTP, CharConstants.HTTPS, CharConstants.WWW, "."},
                new String[]{"", "", "", "/"});
    }
}
