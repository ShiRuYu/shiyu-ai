package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.implementation.domain.chat.CharacterAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.GroupChatAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.LorebookAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.PersonaAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.PromptTemplateVersion;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * ChatProductRepository 仓储接口，负责访问和持久化会话领域聚合数据。
 */
public interface ChatProductRepository {
    /**
     * 保存或更新业务对象。
     *
     * @param asset 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    CharacterAsset saveCharacter(CharacterAsset asset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    Optional<CharacterAsset> findCharacter(TenantId tenantId, long ownerUserId, String id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param requesterUserId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    default Optional<CharacterAsset> findCharacterForAccess(
            TenantId tenantId, long requesterUserId, String id) {
        return findCharacter(tenantId, requesterUserId, id);
    }

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<CharacterAsset> listCharacters(TenantId tenantId, long ownerUserId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     */
    void deleteCharacter(TenantId tenantId, long ownerUserId, String id);

    /**
     * 保存或更新业务对象。
     *
     * @param asset 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    PersonaAsset savePersona(PersonaAsset asset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    Optional<PersonaAsset> findPersona(TenantId tenantId, long ownerUserId, String id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<PersonaAsset> listPersonas(TenantId tenantId, long ownerUserId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     */
    void deletePersona(TenantId tenantId, long ownerUserId, String id);

    /**
     * 保存或更新业务对象。
     *
     * @param asset 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    LorebookAsset saveLorebook(LorebookAsset asset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    Optional<LorebookAsset> findLorebook(TenantId tenantId, long ownerUserId, String id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<LorebookAsset> listLorebooks(TenantId tenantId, long ownerUserId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     */
    void deleteLorebook(TenantId tenantId, long ownerUserId, String id);

    /**
     * 保存或更新业务对象。
     *
     * @param version 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作结果。
     */
    PromptTemplateVersion savePrompt(
            PromptTemplateVersion version, TenantId tenantId, long ownerUserId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param templateId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<PromptTemplateVersion> listPrompts(TenantId tenantId, long ownerUserId, String templateId);

    /**
     * 保存或更新业务对象。
     *
     * @param asset 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    GroupChatAsset saveGroup(GroupChatAsset asset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    Optional<GroupChatAsset> findGroup(TenantId tenantId, long ownerUserId, String id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<GroupChatAsset> listGroups(TenantId tenantId, long ownerUserId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param id 目标对象标识。
     */
    void deleteGroup(TenantId tenantId, long ownerUserId, String id);
}
