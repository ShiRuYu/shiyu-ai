package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.application.assembler.AuthCodeAssembler;
import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import com.shiyu.ai.auth.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.auth.port.repository.AuthCodeRepository;
import com.shiyu.ai.auth.port.repository.RoleRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.service.AuthCodeService;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Authorization-code application service. */
@Service
public class AuthCodeServiceImpl implements AuthCodeService {
    private final AuthCodeRepository authCodeRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    public AuthCodeServiceImpl(AuthCodeRepository authCodeRepository,
                               RoleRepository roleRepository,
                               TenantRepository tenantRepository) {
        this.authCodeRepository = authCodeRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public List<AuthCodeOptionVO> list(ActorContext actor) {
        actor = requireActor(actor);
        return authCodeRepository.selectByTenantId(actor.tenantId())
                .stream().map(this::toOption).toList();
    }

    @Override
    public List<String> listRoleAuthCodes(ActorContext actor, Long roleId, TenantId tenantId) {
        actor = requireActor(actor);
        if (!isValidScope(actor, roleId, tenantId)) {
            throw new IllegalArgumentException("角色不属于当前租户作用域");
        }
        return authCodeRepository.selectByRoleIdAndTenantId(roleId, tenantId)
                .stream().map(AuthCodeBO::getCode).filter(Objects::nonNull).distinct().toList();
    }

    @Override
    public List<AuthCodeOptionVO> options(ActorContext actor) {
        return list(actor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthCodeResponse create(ActorContext actor, AuthCodeRequest request) {
        actor = requireActor(actor);
        AuthCodeBO authCode = AuthCodeAssembler.toBO(request);
        validateCode(authCode);
        if (authCodeRepository.existsByCode(authCode.getCode().trim(), null)) {
            throw new IllegalArgumentException("权限编码已存在");
        }
        TenantId tenantId = actor.tenantId();
        authCode.setStatus(1);
        authCode.setDelFlag(0);
        authCode.setCreateTime(LocalDateTime.now());
        AuthCodeBO saved = authCodeRepository.insert(authCode);
        TenantAuthCodeBO assignment = new TenantAuthCodeBO();
        assignment.setTenantId(tenantId.value());
        assignment.setAuthCodeId(saved.getId());
        assignment.setStatus(1);
        authCodeRepository.insertTenantCode(assignment);
        return AuthCodeAssembler.toResponse(saved);
    }

    @Override
    public boolean update(ActorContext actor, Long id, AuthCodeRequest request) {
        actor = requireActor(actor);
        AuthCodeBO incoming = AuthCodeAssembler.toBO(request);
        AuthCodeBO existing = authCodeRepository.selectById(id);
        TenantId tenantId = actor.tenantId();
        if (existing == null || !authCodeRepository.isAvailable(id, tenantId)) {
            return false;
        }
        validateCode(incoming);
        if (authCodeRepository.existsByCode(incoming.getCode().trim(), id)) {
            throw new IllegalArgumentException("权限编码已存在");
        }
        incoming.setId(id);
        incoming.setStatus(existing.getStatus());
        incoming.setDelFlag(existing.getDelFlag());
        incoming.setCreateTime(existing.getCreateTime());
        incoming.setUpdateTime(LocalDateTime.now());
        authCodeRepository.update(incoming);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(ActorContext actor, Long id) {
        actor = requireActor(actor);
        AuthCodeBO existing = authCodeRepository.selectById(id);
        TenantId tenantId = actor.tenantId();
        if (existing == null || !authCodeRepository.isAvailable(id, tenantId)) {
            return false;
        }
        if (authCodeRepository.hasRoleAssignments(id)) {
            throw new IllegalStateException("权限码已分配给角色，请先取消授权");
        }
        authCodeRepository.deleteTenantCode(tenantId, id);
        if (authCodeRepository.countActiveTenantLinks(id) == 0) {
            existing.setDelFlag(1);
            existing.setUpdateTime(LocalDateTime.now());
            authCodeRepository.update(existing);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grant(ActorContext actor, Long roleId, TenantId tenantId, List<Long> authCodeIds) {
        actor = requireActor(actor);
        if (!isValidScope(actor, roleId, tenantId)
                || authCodeIds == null || authCodeIds.isEmpty()) {
            return false;
        }
        List<Long> distinctIds = authCodeIds.stream().filter(Objects::nonNull).distinct().toList();
        List<AuthCodeBO> valid = authCodeRepository.selectAvailableByIds(distinctIds, tenantId);
        if (valid.size() != distinctIds.size()) {
            return false;
        }
        Set<Long> existing = new HashSet<>(authCodeRepository.selectByRoleIdAndTenantId(roleId, tenantId)
                .stream().map(AuthCodeBO::getId).toList());
        LocalDateTime now = LocalDateTime.now();
        List<RoleScopeAuthCodeBO> records = distinctIds.stream().filter(id -> !existing.contains(id)).map(id -> {
            RoleScopeAuthCodeBO item = new RoleScopeAuthCodeBO();
            item.setRoleId(roleId);
            item.setAuthCodeId(id);
            item.setTenantId(tenantId.value());
            item.setStatus(1);
            item.setDelFlag(0);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            return item;
        }).toList();
        authCodeRepository.insertRoleAssignments(records);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replace(ActorContext actor, Long roleId, TenantId tenantId, List<String> authCodes) {
        actor = requireActor(actor);
        if (!isValidScope(actor, roleId, tenantId)) {
            return false;
        }
        List<String> target = authCodes == null ? List.of() : authCodes.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        List<AuthCodeBO> valid = target.isEmpty() ? List.of() : authCodeRepository.selectByTenantId(tenantId).stream()
                .filter(a -> target.contains(a.getCode())).toList();
        if (valid.size() != target.size()) {
            return false;
        }
        authCodeRepository.deleteRoleAssignments(roleId, tenantId, null);
        if (!valid.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            authCodeRepository.insertRoleAssignments(valid.stream().map(a -> {
                RoleScopeAuthCodeBO item = new RoleScopeAuthCodeBO();
                item.setRoleId(roleId);
                item.setAuthCodeId(a.getId());
                item.setTenantId(tenantId.value());
                item.setStatus(1);
                item.setDelFlag(0);
                item.setCreateTime(now);
                item.setUpdateTime(now);
                return item;
            }).toList());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(ActorContext actor, Long roleId, TenantId tenantId, Long authCodeId) {
        actor = requireActor(actor);
        if (!isValidScope(actor, roleId, tenantId)) {
            return false;
        }
        authCodeRepository.deleteRoleAssignments(roleId, tenantId, authCodeId);
        return true;
    }

    @Override
    public PageData<AuthCodeOptionVO> page(ActorContext actor, AuthCodePageRequest request) {
        actor = requireActor(actor);
        List<AuthCodeOptionVO> items = authCodeRepository.selectByTenantId(actor.tenantId()).stream()
                .filter(a -> request.getCode() == null || request.getCode().isBlank()
                        || (a.getCode() != null && a.getCode().contains(request.getCode())))
                .filter(a -> request.getName() == null || request.getName().isBlank()
                        || (a.getName() != null && a.getName().contains(request.getName())))
                .sorted(Comparator.comparing(AuthCodeBO::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toOption).toList();
        int page = request.getPageNum() == null ? 1 : request.getPageNum();
        int size = request.getPageSize() == null ? 10 : request.getPageSize();
        int from = Math.min(Math.max(0, (page - 1) * size), items.size());
        int to = Math.min(from + size, items.size());
        return new PageData<>(items.subList(from, to), items.size());
    }

    private void validateCode(AuthCodeBO authCode) {
        if (authCode == null || authCode.getCode() == null || authCode.getCode().isBlank()
                || authCode.getCode().length() > 64) {
            throw new IllegalArgumentException("权限编码不能为空且长度不能超过64");
        }
    }

    private AuthCodeOptionVO toOption(AuthCodeBO authCode) {
        String[] parts = authCode.getCode() == null ? new String[0] : authCode.getCode().split(":");
        AuthCodeOptionVO option = new AuthCodeOptionVO();
        option.setId(authCode.getId());
        option.setName(authCode.getName());
        option.setCode(authCode.getCode());
        option.setModule(parts.length > 0 ? parts[0] : "");
        option.setAction(parts.length > 1 ? parts[parts.length - 1] : "");
        option.setResource(parts.length > 2 ? String.join(":", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1)) : "");
        option.setStatus(authCode.getStatus());
        option.setCreateTime(authCode.getCreateTime());
        return option;
    }

    private boolean isValidScope(ActorContext actor, Long roleId, TenantId tenantId) {
        Long currentTenantId = actor.tenantId().value();
        if (tenantId == null) return false;
        Long requestedTenantId = tenantId.value();
        if (!roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            return false;
        }
        var tenant = tenantRepository.selectById(requestedTenantId);
        return tenant != null && tenant.getStatus() != null && tenant.getStatus() == 1
                && (tenant.getDelFlag() == null || tenant.getDelFlag() == 0)
                && tenantRepository.selectDescendantIds(new TenantId(currentTenantId)).contains(requestedTenantId);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return Objects.requireNonNull(actor, "actor context is required");
    }
}
