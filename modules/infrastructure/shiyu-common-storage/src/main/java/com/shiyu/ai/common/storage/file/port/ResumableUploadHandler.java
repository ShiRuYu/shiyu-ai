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
 * ResumableUploadHandler 接口，定义基础设施模块的能力边界。
 */
public interface ResumableUploadHandler {

    /**
     * 执行 {@code authorize} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     */
    void authorize(UploadActor actor, Long spaceId);

    /**
     * 执行 {@code namespace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    String namespace(TenantId tenantId, Long spaceId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    RegistrationResult register(UploadActor actor, UploadRegistration request);

    /**
     * {@code UploadActor} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param userId 用户标识，表示该记录组件承载的数据。
     * @param roleId roleId 属性，表示该记录组件承载的数据。
     * @param platformAdmin platformAdmin 属性，表示该记录组件承载的数据。
     */
    record UploadActor(TenantId tenantId, UserId userId, RoleId roleId, boolean platformAdmin) {
        public UploadActor {
            if (tenantId == null || userId == null) {
                throw new IllegalArgumentException("tenantId and userId are required");
            }
        }
    }

    /**
     * {@code UploadRegistration} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param storageProvider storageProvider 属性，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
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
     * {@code RegistrationResult} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param value 值，表示该记录组件承载的数据。
     * @param duplicate duplicate 属性，表示该记录组件承载的数据。
     */
    record RegistrationResult(Object value, boolean duplicate) {}
}
