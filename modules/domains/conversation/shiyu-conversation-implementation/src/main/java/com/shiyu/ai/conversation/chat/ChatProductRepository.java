package com.shiyu.ai.conversation.chat;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Optional;

public interface ChatProductRepository {
    CharacterAsset saveCharacter(CharacterAsset asset);
    Optional<CharacterAsset> findCharacter(TenantId tenantId, long ownerUserId, String id);
    default Optional<CharacterAsset> findCharacterForAccess(TenantId tenantId, long requesterUserId, String id) { return findCharacter(tenantId, requesterUserId, id); }
    List<CharacterAsset> listCharacters(TenantId tenantId, long ownerUserId);
    void deleteCharacter(TenantId tenantId, long ownerUserId, String id);

    PersonaAsset savePersona(PersonaAsset asset);
    Optional<PersonaAsset> findPersona(TenantId tenantId, long ownerUserId, String id);
    List<PersonaAsset> listPersonas(TenantId tenantId, long ownerUserId);
    void deletePersona(TenantId tenantId, long ownerUserId, String id);

    LorebookAsset saveLorebook(LorebookAsset asset);
    Optional<LorebookAsset> findLorebook(TenantId tenantId, long ownerUserId, String id);
    List<LorebookAsset> listLorebooks(TenantId tenantId, long ownerUserId);
    void deleteLorebook(TenantId tenantId, long ownerUserId, String id);

    PromptTemplateVersion savePrompt(PromptTemplateVersion version, TenantId tenantId, long ownerUserId);
    List<PromptTemplateVersion> listPrompts(TenantId tenantId, long ownerUserId, String templateId);

    GroupChatAsset saveGroup(GroupChatAsset asset);
    Optional<GroupChatAsset> findGroup(TenantId tenantId, long ownerUserId, String id);
    List<GroupChatAsset> listGroups(TenantId tenantId, long ownerUserId);
    void deleteGroup(TenantId tenantId, long ownerUserId, String id);
}
