package com.shiyu.ai.chat.product;

import java.util.List;
import java.util.Optional;

public interface ChatProductRepository {
    CharacterAsset saveCharacter(CharacterAsset asset);
    Optional<CharacterAsset> findCharacter(long tenantId, long ownerUserId, String id);
    default Optional<CharacterAsset> findCharacterForAccess(long tenantId, long requesterUserId, String id) { return findCharacter(tenantId, requesterUserId, id); }
    List<CharacterAsset> listCharacters(long tenantId, long ownerUserId);
    void deleteCharacter(long tenantId, long ownerUserId, String id);

    PersonaAsset savePersona(PersonaAsset asset);
    Optional<PersonaAsset> findPersona(long tenantId, long ownerUserId, String id);
    List<PersonaAsset> listPersonas(long tenantId, long ownerUserId);
    void deletePersona(long tenantId, long ownerUserId, String id);

    LorebookAsset saveLorebook(LorebookAsset asset);
    Optional<LorebookAsset> findLorebook(long tenantId, long ownerUserId, String id);
    List<LorebookAsset> listLorebooks(long tenantId, long ownerUserId);
    void deleteLorebook(long tenantId, long ownerUserId, String id);

    PromptTemplateVersion savePrompt(PromptTemplateVersion version, long tenantId, long ownerUserId);
    List<PromptTemplateVersion> listPrompts(long tenantId, long ownerUserId, String templateId);

    default GroupChatAsset saveGroup(GroupChatAsset asset) { throw new UnsupportedOperationException("group chat persistence is not configured"); }
    default Optional<GroupChatAsset> findGroup(long tenantId, long ownerUserId, String id) { return Optional.empty(); }
    default List<GroupChatAsset> listGroups(long tenantId, long ownerUserId) { return List.of(); }
    default void deleteGroup(long tenantId, long ownerUserId, String id) { }
}
