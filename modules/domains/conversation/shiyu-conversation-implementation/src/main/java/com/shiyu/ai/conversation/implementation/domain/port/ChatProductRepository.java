package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.model.GroupChatAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.model.LorebookAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.model.PersonaAsset;
import com.shiyu.ai.conversation.implementation.domain.chat.model.PromptTemplateVersion;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 对话 Product 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ChatProductRepository {
    /**
     * 创建或保存 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 对话 Product 相关操作生成的结果数据。
     */
    CharacterAsset saveCharacter(CharacterAsset asset);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<CharacterAsset> findCharacter(TenantId tenantId, long ownerUserId, String id);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param requesterUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    default Optional<CharacterAsset> findCharacterForAccess(
            TenantId tenantId, long requesterUserId, String id) {
        return findCharacter(tenantId, requesterUserId, id);
    }

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<CharacterAsset> listCharacters(TenantId tenantId, long ownerUserId);

    /**
     * 删除或移除 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deleteCharacter(TenantId tenantId, long ownerUserId, String id);

    /**
     * 创建或保存 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 对话 Product 相关操作生成的结果数据。
     */
    PersonaAsset savePersona(PersonaAsset asset);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<PersonaAsset> findPersona(TenantId tenantId, long ownerUserId, String id);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<PersonaAsset> listPersonas(TenantId tenantId, long ownerUserId);

    /**
     * 删除或移除 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deletePersona(TenantId tenantId, long ownerUserId, String id);

    /**
     * 创建或保存 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 对话 Product 相关操作生成的结果数据。
     */
    LorebookAsset saveLorebook(LorebookAsset asset);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<LorebookAsset> findLorebook(TenantId tenantId, long ownerUserId, String id);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<LorebookAsset> listLorebooks(TenantId tenantId, long ownerUserId);

    /**
     * 删除或移除 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deleteLorebook(TenantId tenantId, long ownerUserId, String id);

    /**
     * 创建或保存 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 对话 Product 相关操作生成的结果数据。
     */
    PromptTemplateVersion savePrompt(
            PromptTemplateVersion version, TenantId tenantId, long ownerUserId);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param templateId 用于定位template的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<PromptTemplateVersion> listPrompts(TenantId tenantId, long ownerUserId, String templateId);

    /**
     * 创建或保存 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 对话 Product 相关操作生成的结果数据。
     */
    GroupChatAsset saveGroup(GroupChatAsset asset);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<GroupChatAsset> findGroup(TenantId tenantId, long ownerUserId, String id);

    /**
     * 查询 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<GroupChatAsset> listGroups(TenantId tenantId, long ownerUserId);

    /**
     * 删除或移除 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deleteGroup(TenantId tenantId, long ownerUserId, String id);
}
