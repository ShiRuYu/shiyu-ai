package com.shiyu.ai.common.core.module;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 封装 Business Module Descriptor 相关的不可变数据及其字段约束。
 */
public record BusinessModuleDescriptor(
        String id, String displayName, List<String> routePrefixes, String permissionPrefix) {

    private static final Pattern MODULE_ID = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    public BusinessModuleDescriptor {
        validateId(id);
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("模块名称不能为空");
        }
        routePrefixes =
                routePrefixes == null
                        ? List.of()
                        : routePrefixes.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .toList();
        if (permissionPrefix == null || permissionPrefix.isBlank()) {
            throw new IllegalArgumentException("模块权限前缀不能为空");
        }
        permissionPrefix = permissionPrefix.trim();
    }

    /**
     * 创建并校验模块描述，避免模块 ID 在不同入口使用不同格式。
     *
     * @param id 稳定的模块标识。
     * @param displayName 模块显示名称。
     * @param routePrefix 模块主要 HTTP 路径前缀。
     * @param permissionPrefix 模块权限码前缀。
     * @return 已校验的模块描述。
     */
    public static BusinessModuleDescriptor of(
            String id, String displayName, String routePrefix, String permissionPrefix) {
        return new BusinessModuleDescriptor(
                id,
                displayName,
                routePrefix == null ? List.of() : List.of(routePrefix),
                permissionPrefix);
    }

    /**
     * 校验模块标识是否适合出现在配置键、环境变量和数据库中。
     *
     * @param id 待校验的模块标识。
     */
    public static void validateId(String id) {
        if (id == null || !MODULE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("模块 ID 必须使用小写字母、数字和短横线");
        }
    }
}
