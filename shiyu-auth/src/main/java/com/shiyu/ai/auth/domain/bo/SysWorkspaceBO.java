package com.shiyu.ai.auth.domain.bo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.shiyu.ai.common.core.utils.TreeBuildUtils;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.auth.domain.SysWorkspaceDO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.io.Serial;

/**
 * 工作空间业务对象 sys_workspace
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysWorkspaceDO.class, reverseConvertGenerate = true)
public class SysWorkspaceBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工作空间id
     */
    @NotNull(message = "工作空间id不能为空", groups = { EditGroup.class })
    private Long workspaceId;

    /**
     * 父工作空间ID
     */
    private Long parentId;

    /**
     * 工作空间名称
     */
    @NotBlank(message = "工作空间名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(min = 0, max = 30, message = "工作空间名称长度不能超过{max}个字符")
    private String workspaceName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过{max}个字符")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;

    /**
     * 工作空间状态（1正常 0停用）
     */
    private String status;

    /**
     * 构建前端所需要下拉树结构
     *
     * @param workspaces 工作空间列表
     * @return 下拉树结构列表
     */
    public List<Tree<Long>> buildWorkspaceTreeSelect(List<SysWorkspaceDO> workspaces) {
        if (CollUtil.isEmpty(workspaces)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.build(workspaces, (workspace, tree) ->
                tree.setId(workspace.getWorkspaceId())
                        .setParentId(workspace.getParentId())
                        .setName(workspace.getWorkspaceName())
                        .setWeight(workspace.getOrderNum()));
    }

}
