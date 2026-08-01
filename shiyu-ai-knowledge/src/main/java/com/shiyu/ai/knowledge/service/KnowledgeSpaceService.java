package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public interface KnowledgeSpaceService {

    SpaceView ensureDefaultSpace();

    void initializeTenantDefaults(Long tenantId);

    SpaceView get(Long id);

    DifficultyScaleView difficultyScale(Long spaceId);

    PageData<SpaceView> page(int pageNum, int pageSize, String keyword);

    SpaceView create(CreateSpaceRequest request);

    SpaceView update(Long id, UpdateSpaceRequest request);

    void delete(Long id);

    List<MemberView> members(Long spaceId);

    void replaceMembers(Long spaceId, List<MemberRequest> members);

    void requireAccess(Long spaceId, SpaceRole minimumRole);

    enum SpaceRole {
        VIEWER(1), REVIEWER(2), EDITOR(3), ADMIN(4);

        private final int rank;

        SpaceRole(int rank) {
            this.rank = rank;
        }

        public boolean includes(SpaceRole required) {
            return rank >= required.rank;
        }
    }

    record SpaceView(Long id, String code, String name, String description,
                     String accessMode, String reviewMode, String bindingMode, Long difficultyScaleId,
                     String embeddingProfile,
                     String rerankProfile, String chunkStrategy, Integer chunkSize,
                     Integer chunkOverlap, Long activeIndexVersion, Integer status,
                     LocalDateTime createTime, LocalDateTime updateTime) {
    }

    record MemberView(Long id, Long spaceId, String principalType, Long principalId,
                      String spaceRole) {
    }

    record DifficultyScaleView(Long id, String code, String name, String description,
                               Integer levelCount, List<DifficultyLevelView> levels) {
    }

    record DifficultyLevelView(Integer level, String label, String description) {
    }

    record CreateSpaceRequest(@NotBlank String code, @NotBlank String name,
                              String description, String accessMode, String reviewMode,
                              String bindingMode, Long difficultyScaleId, String embeddingProfile,
                              String rerankProfile,
                              String chunkStrategy, @Min(100) @Max(4000) Integer chunkSize,
                              @Min(0) @Max(1000) Integer chunkOverlap) {
    }

    record UpdateSpaceRequest(String name, String description, String accessMode,
                              String reviewMode, String bindingMode, Long difficultyScaleId,
                              String embeddingProfile,
                              String rerankProfile, String chunkStrategy,
                              @Min(100) @Max(4000) Integer chunkSize,
                              @Min(0) @Max(1000) Integer chunkOverlap,
                              Integer status) {
    }

    record MemberRequest(@NotBlank String principalType, @NotNull Long principalId,
                         @NotBlank String spaceRole) {
    }
}
