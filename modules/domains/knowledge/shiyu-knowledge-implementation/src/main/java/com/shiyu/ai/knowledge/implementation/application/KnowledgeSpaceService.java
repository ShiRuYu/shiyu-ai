package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.KnowledgeTenantProvisioning;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提供 知识 空间 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeSpaceService extends KnowledgeTenantProvisioning {

    /**
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    SpaceView ensureDefaultSpace(ActorContext actor);

    /**
     * 执行 知识 空间 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     */
    @Override
    void initializeTenantDefaults(com.shiyu.ai.kernel.context.TenantId tenantId);

    /**
     * 查询 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    SpaceView get(ActorContext actor, Long id);

    /**
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    DifficultyScaleView difficultyScale(ActorContext actor, Long spaceId);

    /**
     * 查询 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    PageData<SpaceView> page(ActorContext actor, int pageNum, int pageSize, String keyword);

    /**
     * 查询 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param domainCode 用于完成本次业务处理的 domainCode 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    PageData<SpaceView> page(
            ActorContext actor, int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 创建或保存 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    SpaceView create(ActorContext actor, CreateSpaceRequest request);

    /**
     * 更新或设置 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    SpaceView update(ActorContext actor, Long id, UpdateSpaceRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void delete(ActorContext actor, Long id);

    /**
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemberView> members(ActorContext actor, Long spaceId);

    /**
     * 执行 知识 空间 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param members 用于完成本次业务处理的 members 参数。
     */
    void replaceMembers(ActorContext actor, Long spaceId, List<MemberRequest> members);

    /**
     * 获取并校验 知识 空间 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param spaceId 用于定位space的标识。
     * @param minimumRole 用于完成本次业务处理的 minimumRole 参数。
     * @param context 当前操作主体上下文，用于确定租户、用户和访问权限。
     */
    void requireAccess(Long spaceId, SpaceRole minimumRole, ActorContext context);

    /**
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param context 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<SpaceView> accessibleSpaces(ActorContext context);

    /**
     * 定义 空间 角色 可用的枚举值及其业务语义。
     */
    enum SpaceRole {
        VIEWER(1),
        REVIEWER(2),
        EDITOR(3),
        ADMIN(4);

        /**
         * rank 属性，保存当前对象中的业务数据或协作依赖。
         */
        private final int rank;

        SpaceRole(int rank) {
            this.rank = rank;
        }

        /**
         * 执行 空间 角色 相关业务数据，并返回处理结果。
         *
         * @param required 用于完成本次业务处理的 required 参数。
         * @return 返回本次条件判断是否成立。
         */
        public boolean includes(SpaceRole required) {
            return rank >= required.rank;
        }
    }

    /**
     * 封装 空间 View 相关的不可变数据及其字段约束。
     */
    record SpaceView(
            Long id,
            String code,
            String domainCode,
            String name,
            String description,
            String accessMode,
            String reviewMode,
            String bindingMode,
            Long difficultyScaleId,
            String embeddingProfile,
            String rerankProfile,
            String chunkStrategy,
            Integer chunkSize,
            Integer chunkOverlap,
            Long activeIndexVersion,
            Integer status,
            LocalDateTime createTime,
            LocalDateTime updateTime) {}

    /**
     * 封装 Member View 相关的不可变数据及其字段约束。
     */
    record MemberView(
            Long id, Long spaceId, String principalType, Long principalId, String spaceRole) {}

    /**
     * 封装 Difficulty Scale View 相关的不可变数据及其字段约束。
     */
    record DifficultyScaleView(
            Long id,
            String code,
            String name,
            String description,
            Integer levelCount,
            List<DifficultyLevelView> levels) {}

    /**
     * 封装 Difficulty Level View 相关的不可变数据及其字段约束。
     */
    record DifficultyLevelView(Integer level, String label, String description) {}

    /**
     * 封装 Create 空间 相关的不可变数据及其字段约束。
     */
    record CreateSpaceRequest(
            @NotBlank String code,
            @NotBlank String name,
            String domainCode,
            String description,
            String accessMode,
            String reviewMode,
            String bindingMode,
            Long difficultyScaleId,
            String embeddingProfile,
            String rerankProfile,
            String chunkStrategy,
            @Min(100) @Max(4000) Integer chunkSize,
            @Min(0) @Max(1000) Integer chunkOverlap) {}


    /**
     * 封装知识空间更新所需的名称、访问策略、索引策略和状态。
     */
    record UpdateSpaceRequest(
            String name,
            String description,
            String domainCode,
            String accessMode,
            String reviewMode,
            String bindingMode,
            Long difficultyScaleId,
            String embeddingProfile,
            String rerankProfile,
            String chunkStrategy,
            @Min(100) @Max(4000) Integer chunkSize,
            @Min(0) @Max(1000) Integer chunkOverlap,
            Integer status) {}

    /**
     * 封装 Member 相关的不可变数据及其字段约束。
     */
    record MemberRequest(
            @NotBlank String principalType,
            @NotNull Long principalId,
            @NotBlank String spaceRole) {}
}
