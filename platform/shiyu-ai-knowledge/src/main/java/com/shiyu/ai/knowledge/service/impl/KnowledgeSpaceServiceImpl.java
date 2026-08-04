package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDifficultyScaleRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.security.KnowledgeAccessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KnowledgeSpaceServiceImpl implements KnowledgeSpaceService {

    private static final Set<String> ACCESS_MODES = Set.of("PRIVATE", "TENANT");
    private static final Set<String> REVIEW_MODES = Set.of("DIRECT", "OPTIONAL", "REQUIRED");
    private static final Set<String> BINDING_MODES = Set.of("OPTIONAL", "REQUIRED");
    private static final Set<String> PRINCIPAL_TYPES = Set.of("USER", "ROLE");
    private static final String GENERAL_DOMAIN = "GENERAL";

    private final KnowledgeEnterpriseRepository repository;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeRelationRepository relationRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDifficultyScaleRepository difficultyScaleRepository;
    private final KnowledgeDocRelationRepository docRelationRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeAuditService auditService;

    @Value("${shiyu.knowledge.default-space-code:default}")
    private String defaultSpaceCode;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpaceView ensureDefaultSpace() {
        requireTenant();
        KnowledgeSpaceBO existing = repository.findSpaceByCode(defaultSpaceCode);
        if (existing != null) {
            if (existing.getDomainCode() == null || existing.getDomainCode().isBlank()) {
                existing.setDomainCode(GENERAL_DOMAIN);
                repository.updateSpace(existing);
            }
            assignLegacyData(existing.getId());
            return toView(existing);
        }
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setCode(defaultSpaceCode);
        space.setDomainCode(GENERAL_DOMAIN);
        space.setName("默认知识空间");
        space.setDescription("兼容既有知识点、文档和教育关联的默认空间");
        space.setAccessMode("TENANT");
        space.setReviewMode("DIRECT");
        space.setBindingMode("OPTIONAL");
        space.setDifficultyScaleId(1L);
        applyDefaults(space);
        repository.insertSpace(space);
        assignLegacyData(space.getId());
        auditService.record(space.getId(), "SPACE", space.getId(), "CREATE_DEFAULT", null);
        return toView(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeTenantDefaults(Long tenantId) {
        if (tenantId == null) {
            throw new ServiceException("tenantId must not be null");
        }
        if (repository.findSpaceByTenantAndCode(tenantId, defaultSpaceCode) != null) {
            return;
        }
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setTenantId(tenantId);
        space.setCode(defaultSpaceCode);
        space.setDomainCode(GENERAL_DOMAIN);
        space.setName("企业通用知识空间");
        space.setDescription("租户默认的企业通用知识空间");
        space.setAccessMode("TENANT");
        space.setReviewMode("DIRECT");
        applyDefaults(space);
        repository.insertSpace(space);
    }

    @Override
    public SpaceView get(Long id) {
        requireAccess(id, SpaceRole.VIEWER);
        KnowledgeSpaceBO space = requireSpace(id);
        return toView(space);
    }

    @Override
    public DifficultyScaleView difficultyScale(Long spaceId) {
        requireAccess(spaceId, SpaceRole.VIEWER);
        KnowledgeSpaceBO space = requireSpace(spaceId);
        var scale = difficultyScaleRepository.findScale(space.getDifficultyScaleId());
        if (scale == null) {
            throw new ServiceException("知识空间未配置有效的难度量表");
        }
        List<DifficultyLevelView> levels = difficultyScaleRepository.findLevels(scale.getId()).stream()
                .map(level -> new DifficultyLevelView(level.getLevel(), level.getLabel(),
                        level.getDescription()))
                .toList();
        return new DifficultyScaleView(scale.getId(), scale.getCode(), scale.getName(),
                scale.getDescription(), scale.getLevelCount(), levels);
    }

    @Override
    public PageData<SpaceView> page(int pageNum, int pageSize, String keyword) {
        return page(pageNum, pageSize, keyword, null);
    }

    @Override
    public PageData<SpaceView> page(int pageNum, int pageSize, String keyword, String domainCode) {
        String normalizedDomain = normalizeDomainCode(domainCode, null);
        PageData<KnowledgeSpaceBO> page = repository.pageSpaces(pageNum, pageSize, keyword,
                normalizedDomain);
        List<SpaceView> visible = page.getItems().stream()
                .filter(this::canView)
                .map(this::toView)
                .toList();
        return new PageData<>(visible, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpaceView create(CreateSpaceRequest request) {
        requireTenant();
        String code = normalizeCode(request.code());
        if (repository.findSpaceByCode(code) != null) {
            throw new ServiceException("知识空间编码已存在: " + code);
        }
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setCode(code);
        space.setDomainCode(normalizeDomainCode(request.domainCode(), GENERAL_DOMAIN));
        space.setName(request.name().trim());
        space.setDescription(request.description());
        space.setAccessMode(normalizeEnum(request.accessMode(), "PRIVATE", ACCESS_MODES, "访问模式"));
        space.setReviewMode(normalizeEnum(request.reviewMode(), "OPTIONAL", REVIEW_MODES, "审核模式"));
        space.setDifficultyScaleId(request.difficultyScaleId() == null ? 1L : request.difficultyScaleId());
        space.setBindingMode(normalizeEnum(request.bindingMode(), "OPTIONAL", BINDING_MODES, "binding mode"));
        space.setEmbeddingProfile(defaultText(request.embeddingProfile(), "default"));
        space.setRerankProfile(defaultText(request.rerankProfile(), "default"));
        space.setChunkStrategy(defaultText(request.chunkStrategy(), "HEADING").toUpperCase(Locale.ROOT));
        space.setChunkSize(request.chunkSize() == null ? 800 : request.chunkSize());
        space.setChunkOverlap(request.chunkOverlap() == null ? 100 : request.chunkOverlap());
        space.setActiveIndexVersion(0L);
        space.setStatus(1);
        space.setDelFlag(0);
        repository.insertSpace(space);

        Long userId = UserContextHolder.getUserId();
        if (userId != null) {
            KnowledgeSpaceMemberBO owner = new KnowledgeSpaceMemberBO();
            owner.setSpaceId(space.getId());
            owner.setPrincipalType("USER");
            owner.setPrincipalId(userId);
            owner.setSpaceRole("ADMIN");
            owner.setStatus(1);
            owner.setDelFlag(0);
            repository.replaceMembers(space.getId(), List.of(owner));
        }
        auditService.record(space.getId(), "SPACE", space.getId(), "CREATE", request);
        return toView(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpaceView update(Long id, UpdateSpaceRequest request) {
        requireAccess(id, SpaceRole.ADMIN);
        KnowledgeSpaceBO space = requireSpace(id);
        if (request.name() != null && !request.name().isBlank()) {
            space.setName(request.name().trim());
        }
        if (request.description() != null) {
            space.setDescription(request.description());
        }
        if (request.domainCode() != null) {
            space.setDomainCode(normalizeDomainCode(request.domainCode(), null));
        }
        if (request.accessMode() != null) {
            space.setAccessMode(normalizeEnum(request.accessMode(), null, ACCESS_MODES, "访问模式"));
        }
        if (request.reviewMode() != null) {
            space.setReviewMode(normalizeEnum(request.reviewMode(), null, REVIEW_MODES, "审核模式"));
        }
        if (request.difficultyScaleId() != null) {
            space.setDifficultyScaleId(request.difficultyScaleId());
        }
        if (request.bindingMode() != null) {
            space.setBindingMode(normalizeEnum(request.bindingMode(), null, BINDING_MODES, "binding mode"));
        }
        if (request.embeddingProfile() != null) {
            space.setEmbeddingProfile(request.embeddingProfile());
        }
        if (request.rerankProfile() != null) {
            space.setRerankProfile(request.rerankProfile());
        }
        if (request.chunkStrategy() != null) {
            space.setChunkStrategy(request.chunkStrategy().toUpperCase(Locale.ROOT));
        }
        if (request.chunkSize() != null) {
            space.setChunkSize(request.chunkSize());
        }
        if (request.chunkOverlap() != null) {
            space.setChunkOverlap(request.chunkOverlap());
        }
        if (request.status() != null) {
            space.setStatus(request.status());
        }
        repository.updateSpace(space);
        auditService.record(id, "SPACE", id, "UPDATE", request);
        return toView(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAccess(id, SpaceRole.ADMIN);
        KnowledgeSpaceBO space = requireSpace(id);
        if (defaultSpaceCode.equals(space.getCode())) {
            throw new ServiceException("默认知识空间不能删除");
        }
        boolean hasKnowledge = !knowledgeRepository.findBySpace(id).isEmpty();
        boolean hasDocuments = !documentRepository.findBySpace(id).isEmpty();
        if (hasKnowledge || hasDocuments) {
            throw new ServiceException("知识空间仍包含知识点或文档，请先删除内容后再删除空间");
        }
        repository.deleteSpace(id);
        auditService.record(id, "SPACE", id, "DELETE", null);
    }

    @Override
    public List<MemberView> members(Long spaceId) {
        requireAccess(spaceId, SpaceRole.ADMIN);
        return repository.findMembers(spaceId).stream()
                .map(member -> new MemberView(member.getId(), member.getSpaceId(),
                        member.getPrincipalType(), member.getPrincipalId(), member.getSpaceRole()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceMembers(Long spaceId, List<MemberRequest> requests) {
        requireAccess(spaceId, SpaceRole.ADMIN);
        List<KnowledgeSpaceMemberBO> members = new ArrayList<>();
        for (MemberRequest request : requests == null ? List.<MemberRequest>of() : requests) {
            String principalType = request.principalType().toUpperCase(Locale.ROOT);
            if (!PRINCIPAL_TYPES.contains(principalType)) {
                throw new ServiceException("不支持的授权主体: " + principalType);
            }
            SpaceRole role;
            try {
                role = SpaceRole.valueOf(request.spaceRole().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new ServiceException("不支持的空间角色: " + request.spaceRole());
            }
            KnowledgeSpaceMemberBO member = new KnowledgeSpaceMemberBO();
            member.setSpaceId(spaceId);
            member.setPrincipalType(principalType);
            member.setPrincipalId(request.principalId());
            member.setSpaceRole(role.name());
            member.setStatus(1);
            member.setDelFlag(0);
            members.add(member);
        }
        if (members.stream().noneMatch(member -> "ADMIN".equals(member.getSpaceRole()))) {
            throw new ServiceException("知识空间至少需要一个管理员");
        }
        repository.replaceMembers(spaceId, members);
        auditService.record(spaceId, "SPACE", spaceId, "REPLACE_MEMBERS", requests);
    }

    @Override
    public void requireAccess(Long spaceId, SpaceRole minimumRole) {
        KnowledgeSpaceBO space = requireSpace(spaceId);
        if (UserContextHolder.isSuperAdmin()) {
            return;
        }
        if (minimumRole == SpaceRole.VIEWER && "TENANT".equals(space.getAccessMode())) {
            return;
        }
        Long userId = UserContextHolder.getUserId();
        Long roleId = UserContextHolder.getCurrentRoleId();
        List<String> acceptedRoles = java.util.Arrays.stream(SpaceRole.values())
                .filter(role -> role.includes(minimumRole))
                .map(Enum::name)
                .toList();
        if (repository.hasMember(spaceId, "USER", userId, acceptedRoles)
                || repository.hasMember(spaceId, "ROLE", roleId, acceptedRoles)) {
            return;
        }
        throw new ServiceException("无权访问该知识空间");
    }

    @Override
    public void requireAccess(Long spaceId, SpaceRole minimumRole, KnowledgeAccessContext context) {
        if (context == null || context.tenantId() == null) {
            throw new ServiceException("当前租户上下文不存在");
        }
        KnowledgeSpaceBO space = repository.findSpaceByTenant(context.tenantId(), spaceId);
        if (space == null) {
            throw new ServiceException("知识空间不存在或不属于当前租户: " + spaceId);
        }
        if (context.superAdmin()) {
            return;
        }
        if (minimumRole == SpaceRole.VIEWER && "TENANT".equals(space.getAccessMode())) {
            return;
        }
        List<String> acceptedRoles = java.util.Arrays.stream(SpaceRole.values())
                .filter(role -> role.includes(minimumRole))
                .map(Enum::name)
                .toList();
        if (repository.hasMember(context.tenantId(), spaceId, "USER", context.userId(), acceptedRoles)
                || repository.hasMember(context.tenantId(), spaceId, "ROLE", context.roleId(), acceptedRoles)) {
            return;
        }
        throw new ServiceException("无权访问该知识空间");
    }

    @Override
    public List<SpaceView> accessibleSpaces(KnowledgeAccessContext context) {
        if (context == null || context.tenantId() == null) {
            return List.of();
        }
        return repository.findActiveSpacesByTenant(context.tenantId()).stream()
                .filter(space -> {
                    try {
                        requireAccess(space.getId(), SpaceRole.VIEWER, context);
                        return true;
                    } catch (ServiceException ignored) {
                        return false;
                    }
                })
                .map(this::toView)
                .toList();
    }

    private boolean canView(KnowledgeSpaceBO space) {
        try {
            requireAccess(space.getId(), SpaceRole.VIEWER);
            return true;
        } catch (ServiceException ignored) {
            return false;
        }
    }

    private KnowledgeSpaceBO requireSpace(Long id) {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        KnowledgeSpaceBO space = tenantId == null ? null : repository.findSpaceByTenant(tenantId, id);
        if (space == null) {
            throw new ServiceException("知识空间不存在: " + id);
        }
        return space;
    }

    private void assignLegacyData(Long spaceId) {
        knowledgeRepository.assignDefaultSpace(spaceId);
        relationRepository.assignDefaultSpace(spaceId);
        documentRepository.assignDefaultSpace(spaceId);
        docRelationRepository.assignDefaultSpace(spaceId);
        chunkRepository.assignDefaultSpace(spaceId);
    }

    private void applyDefaults(KnowledgeSpaceBO space) {
        space.setBindingMode("OPTIONAL");
        space.setEmbeddingProfile("default");
        space.setRerankProfile("default");
        space.setChunkStrategy("HEADING");
        space.setChunkSize(800);
        space.setChunkOverlap(100);
        space.setActiveIndexVersion(0L);
        space.setDifficultyScaleId(1L);
        space.setStatus(1);
        space.setDelFlag(0);
    }

    private Long requireTenant() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new ServiceException("当前租户上下文不存在");
        }
        return tenantId;
    }

    private String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }

    private String normalizeEnum(String value, String defaultValue,
                                 Set<String> values, String label) {
        String normalized = value == null || value.isBlank()
                ? defaultValue : value.toUpperCase(Locale.ROOT);
        if (normalized == null || !values.contains(normalized)) {
            throw new ServiceException(label + "不合法: " + value);
        }
        return normalized;
    }

    private String normalizeDomainCode(String value, String defaultValue) {
        String normalized = value == null || value.isBlank()
                ? defaultValue : value.trim().toUpperCase(Locale.ROOT);
        if (normalized == null) {
            return null;
        }
        if (!normalized.matches("[A-Z][A-Z0-9_-]{0,31}")) {
            throw new ServiceException("业务域编码不合法: " + value);
        }
        return normalized;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private SpaceView toView(KnowledgeSpaceBO space) {
        return new SpaceView(space.getId(), space.getCode(), space.getDomainCode(), space.getName(),
                space.getDescription(), space.getAccessMode(), space.getReviewMode(), space.getBindingMode(),
                space.getDifficultyScaleId(),
                space.getEmbeddingProfile(), space.getRerankProfile(),
                space.getChunkStrategy(), space.getChunkSize(), space.getChunkOverlap(),
                space.getActiveIndexVersion(), space.getStatus(),
                space.getCreateTime(), space.getUpdateTime());
    }
}
