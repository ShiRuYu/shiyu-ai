package com.shiyu.ai.agent.implementation.runtime.port;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 工具 Approval 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ToolApprovalRepository {
    /**
     * 创建或保存 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     */
    void insert(ToolApproval approval);

    /**
     * 查询 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId);

    /**
     * 查询 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ToolApproval> listAll(TenantId tenantId, long ownerUserId);

    /**
     * 查询 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 更新或设置 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     * @param expectedStatus 用于完成本次业务处理的 expectedStatus 参数。
     * @return 返回 工具 Approval 相关操作生成的结果数据。
     */
    int update(ToolApproval approval, ToolApprovalStatus expectedStatus);

    /**
     * 执行 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 工具 Approval 相关操作生成的结果数据。
     */
    int expirePending(TenantId tenantId, long ownerUserId);
}
