package com.shiyu.ai.common.core.module;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 根据应用环境中的模块开关决定是否装配业务模块。
 */
public final class BusinessModuleCondition extends SpringBootCondition {

    private static final String PREFIX = "shiyu.modules.";

    @Override
    public ConditionOutcome getMatchOutcome(
            ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes =
                metadata.getAnnotationAttributes(ConditionalOnBusinessModule.class.getName());
        if (attributes == null) {
            return ConditionOutcome.noMatch("缺少业务模块条件注解属性");
        }
        String moduleId = (String) attributes.get("value");
        BusinessModuleDescriptor.validateId(moduleId);
        boolean matchIfMissing = (Boolean) attributes.get("matchIfMissing");
        String propertyName = PREFIX + moduleId + ".enabled";
        String environmentName = "SHIYU_MODULE_" + moduleId.replace('-', '_').toUpperCase() + "_ENABLED";
        String rawValue = context.getEnvironment().getProperty(propertyName);
        if (rawValue == null) {
            rawValue = context.getEnvironment().getProperty(environmentName);
        }
        if (rawValue == null) {
            return matchIfMissing
                    ? ConditionOutcome.match("模块开关未配置，按模块缺省值启用")
                    : ConditionOutcome.noMatch("模块开关未配置，按模块缺省值禁用");
        }
        if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) {
            throw new IllegalStateException(
                    "业务模块开关必须是 true 或 false: " + propertyName);
        }
        boolean enabled = Boolean.parseBoolean(rawValue);
        return enabled
                ? ConditionOutcome.match("模块开关已启用: " + propertyName)
                : ConditionOutcome.noMatch("模块开关已禁用: " + propertyName);
    }
}
