package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessPort;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 校验或约束 Business Module Access 相关的请求、状态和访问规则。
 */
public final class BusinessModuleAccessInterceptor implements HandlerInterceptor {

    private final List<BusinessModuleDescriptor> modules;
    private final TenantModuleAccessPort access;

    /**
     * 创建业务模块访问拦截器。
     *
     * @param modules 当前进程已装配的业务模块描述。
     * @param access IAM 提供的租户模块授权契约。
     */
    public BusinessModuleAccessInterceptor(
            List<BusinessModuleDescriptor> modules, TenantModuleAccessPort access) {
        this.modules = modules == null ? List.of() : List.copyOf(modules);
        this.access = access;
    }

    /**
     * 仅对已装配模块的路由执行租户授权检查。
     *
     * @param request 当前 HTTP 请求。
     * @param response 当前 HTTP 响应。
     * @param handler Spring MVC 处理器。
     * @return 模块已启用或请求不属于业务模块时返回 true。
     * @throws Exception 写入拒绝响应失败时抛出异常。
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (request.getDispatcherType() != DispatcherType.REQUEST
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        BusinessModuleDescriptor module = modules.stream()
                .filter(candidate -> candidate.routePrefixes().stream()
                        .anyMatch(prefix -> matchesPath(path, prefix)))
                .findFirst()
                .orElse(null);
        if (module == null) {
            return true;
        }

        TenantId tenantId = TenantScope.current().orElse(null);
        boolean enabled = tenantId != null && access != null
                && access.isEnabled(tenantId, module.id());
        if (enabled) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(
                JSONUtils.toJsonString(
                        Result.fail(BizResultCode.FORBIDDEN, "当前租户未启用该业务模块")));
        return false;
    }

    private boolean matchesPath(String path, String prefix) {
        return path != null && prefix != null
                && (path.equals(prefix) || path.startsWith(prefix + "/"));
    }
}
