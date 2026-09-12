package com.shiyu.ai.common.core.enums;

import com.shiyu.ai.common.core.CharConstants;
import com.shiyu.ai.common.core.utils.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** todo */
@Getter
@AllArgsConstructor
public enum ComponentTypeEnum {
    /** Layout组件标识 主布局容器 @/layout/index.vue 一级菜单根节点 */
    LAYOUT("Layout"),

    /** ParentView组件标识 中间父级占位符 @/components/ParentView.vue 二级或三级嵌套菜单 */
    PARENT_VIEW("ParentView"),

    /** InnerLink组件标识 内嵌外链 @/components/InnerLink.vue 打开外部或内嵌页面 */
    INNER_LINK("InnerLink");

    /**
     * 值，表示当前对象中的对应属性。
     */
    private final String value;

    /** 根据值获取枚举 */
    public static ComponentTypeEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(componentTypeEnum -> componentTypeEnum.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@code isLayout} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isLayout() {
        return this == LAYOUT;
    }

    /**
     * {@code isParentView} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isParentView() {
        return this == PARENT_VIEW;
    }

    /**
     * {@code isInnerLink} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isInnerLink() {
        return this == INNER_LINK;
    }

    /** 内链域名特殊字符替换 */
    public static String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(
                path,
                new String[] {CharConstants.HTTP, CharConstants.HTTPS, CharConstants.WWW, "."},
                new String[] {"", "", "", "/"});
    }
}
