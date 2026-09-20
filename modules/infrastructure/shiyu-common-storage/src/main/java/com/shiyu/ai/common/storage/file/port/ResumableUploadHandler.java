package com.shiyu.ai.common.storage.file.port;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

/**
 * 处理 Resumable Upload 相关事件或请求，并推进后续业务流程。
 */
public interface ResumableUploadHandler {

    /**
     * 执行 Resumable Upload 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     */
    void authorize(UploadActor actor, Long spaceId);

    /**
     * 执行 Resumable Upload 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 Resumable Upload 相关操作生成的结果数据。
     */
    String namespace(TenantId tenantId, Long spaceId);

    /**
     * 创建或保存 Resumable Upload 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Resumable Upload 相关操作生成的结果数据。
     */
    RegistrationResult register(UploadActor actor, UploadRegistration request);

    /**
     * 封装 Upload Actor 相关的不可变数据及其字段约束。
     */
    record UploadActor(TenantId tenantId, UserId userId, RoleId roleId, boolean platformAdmin) {
        public UploadActor {
            if (tenantId == null || userId == null) {
                throw new IllegalArgumentException("tenantId and userId are required");
            }
        }
    }

    /**
     * 封装 Upload Registration 相关的不可变数据及其字段约束。
     */
    record UploadRegistration(
            TenantId tenantId,
            Long spaceId,
            String title,
            String originalName,
            String objectKey,
            String storageProvider,
            String contentType,
            long size,
            String checksum) {}

    /**
     * 封装 Registration 相关的不可变数据及其字段约束。
     */
    record RegistrationResult(Object value, boolean duplicate) {}
}
