package com.shiyu.ai.auth.domain.vo;

import com.shiyu.ai.auth.domain.bo.SysWorkspaceBO;
import com.shiyu.ai.common.excel.annotation.ExcelDictFormat;
import com.shiyu.ai.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 工作空间视图对象 sys_workspace
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysWorkspaceBO.class)
public class SysWorkspaceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工作空间id
     */
    @ExcelProperty(value = "工作空间id")
    private Long workspaceId;

    /**
     * 父工作空间id
     */
    private Long parentId;

    /**
     * 父工作空间名称
     */
    private String parentName;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 工作空间名称
     */
    @ExcelProperty(value = "工作空间名称")
    private String workspaceName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    @ExcelProperty(value = "负责人")
    private String leader;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String phone;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 工作空间状态（1正常 0停用）
     */
    @ExcelProperty(value = "工作空间状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
