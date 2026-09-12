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
 * KnowledgeSpaceService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeSpaceService extends KnowledgeTenantProvisioning {

    /**
     * 执行 {@code ensureDefaultSpace} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 操作结果。
     */
    SpaceView ensureDefaultSpace(ActorContext actor);

    /**
     * 执行 {@code initializeTenantDefaults} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     */
    @Override
    void initializeTenantDefaults(com.shiyu.ai.kernel.context.TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    SpaceView get(ActorContext actor, Long id);

    /**
     * 执行 {@code difficultyScale} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    DifficultyScaleView difficultyScale(ActorContext actor, Long spaceId);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     *
     * @return 操作结果。
     */
    PageData<SpaceView> page(ActorContext actor, int pageNum, int pageSize, String keyword);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param domainCode 方法参数。
     *
     * @return 操作结果。
     */
    PageData<SpaceView> page(
            ActorContext actor, int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    SpaceView create(ActorContext actor, CreateSpaceRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 操作结果。
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
     * 执行 {@code members} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemberView> members(ActorContext actor, Long spaceId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param members 方法参数。
     */
    void replaceMembers(ActorContext actor, Long spaceId, List<MemberRequest> members);

    /**
     * 执行 {@code requireAccess} 定义的接口操作。
     *
     * @param spaceId 方法参数。
     * @param minimumRole 方法参数。
     * @param context 方法参数。
     */
    void requireAccess(Long spaceId, SpaceRole minimumRole, ActorContext context);

    /**
     * 执行 {@code accessibleSpaces} 定义的接口操作。
     *
     * @param context 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<SpaceView> accessibleSpaces(ActorContext context);

    /**
     * {@code SpaceRole} 表示知识模块中的一组受控业务状态或分类。
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
         * {@code includes} 执行当前类型定义的业务操作。
         *
         * @param required 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public boolean includes(SpaceRole required) {
            return rank >= required.rank;
        }
    }

    /**
     * {@code SpaceView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param code 编码，表示该记录组件承载的数据。
     * @param domainCode domainCode 属性，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param accessMode accessMode 属性，表示该记录组件承载的数据。
     * @param reviewMode reviewMode 属性，表示该记录组件承载的数据。
     * @param bindingMode bindingMode 属性，表示该记录组件承载的数据。
     * @param difficultyScaleId difficultyScaleId 属性，表示该记录组件承载的数据。
     * @param embeddingProfile embeddingProfile 属性，表示该记录组件承载的数据。
     * @param rerankProfile rerankProfile 属性，表示该记录组件承载的数据。
     * @param chunkStrategy chunkStrategy 属性，表示该记录组件承载的数据。
     * @param chunkSize chunkSize 属性，表示该记录组件承载的数据。
     * @param chunkOverlap chunkOverlap 属性，表示该记录组件承载的数据。
     * @param activeIndexVersion activeIndexVersion 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
     * @param createTime createTime 属性，表示该记录组件承载的数据。
     * @param updateTime updateTime 属性，表示该记录组件承载的数据。
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
     * {@code MemberView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param principalType principalType 属性，表示该记录组件承载的数据。
     * @param principalId principalId 属性，表示该记录组件承载的数据。
     * @param spaceRole spaceRole 属性，表示该记录组件承载的数据。
     */
    record MemberView(
            Long id, Long spaceId, String principalType, Long principalId, String spaceRole) {}

    /**
     * {@code DifficultyScaleView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param code 编码，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param levelCount levelCount 属性，表示该记录组件承载的数据。
     * @param levels levels 属性，表示该记录组件承载的数据。
     */
    record DifficultyScaleView(
            Long id,
            String code,
            String name,
            String description,
            Integer levelCount,
            List<DifficultyLevelView> levels) {}

    /**
     * {@code DifficultyLevelView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param level level 属性，表示该记录组件承载的数据。
     * @param label label 属性，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     */
    record DifficultyLevelView(Integer level, String label, String description) {}

    /**
     * {@code CreateSpaceRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param code 编码，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param domainCode domainCode 属性，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param accessMode accessMode 属性，表示该记录组件承载的数据。
     * @param reviewMode reviewMode 属性，表示该记录组件承载的数据。
     * @param bindingMode bindingMode 属性，表示该记录组件承载的数据。
     * @param difficultyScaleId difficultyScaleId 属性，表示该记录组件承载的数据。
     * @param embeddingProfile embeddingProfile 属性，表示该记录组件承载的数据。
     * @param rerankProfile rerankProfile 属性，表示该记录组件承载的数据。
     * @param chunkStrategy chunkStrategy 属性，表示该记录组件承载的数据。
     * @param chunkSize chunkSize 属性，表示该记录组件承载的数据。
     * @param chunkOverlap chunkOverlap 属性，表示该记录组件承载的数据。
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
     * {@code UpdateSpaceRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param domainCode domainCode 属性，表示该记录组件承载的数据。
     * @param accessMode accessMode 属性，表示该记录组件承载的数据。
     * @param reviewMode reviewMode 属性，表示该记录组件承载的数据。
     * @param bindingMode bindingMode 属性，表示该记录组件承载的数据。
     * @param difficultyScaleId difficultyScaleId 属性，表示该记录组件承载的数据。
     * @param embeddingProfile embeddingProfile 属性，表示该记录组件承载的数据。
     * @param rerankProfile rerankProfile 属性，表示该记录组件承载的数据。
     * @param chunkStrategy chunkStrategy 属性，表示该记录组件承载的数据。
     * @param chunkSize chunkSize 属性，表示该记录组件承载的数据。
     * @param chunkOverlap chunkOverlap 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
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
     * {@code MemberRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param principalType principalType 属性，表示该记录组件承载的数据。
     * @param principalId principalId 属性，表示该记录组件承载的数据。
     * @param spaceRole spaceRole 属性，表示该记录组件承载的数据。
     */
    record MemberRequest(
            @NotBlank String principalType,
            @NotNull Long principalId,
            @NotBlank String spaceRole) {}
}
