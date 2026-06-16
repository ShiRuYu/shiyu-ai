package com.shiyu.ai.auth.domain.vo;

import com.shiyu.ai.auth.domain.bo.SysTenantBO;
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
 * 租户视图对象 sys_tenant
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysTenantBO.class)
public class SysTenantVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 租户编码
     */
    @ExcelProperty(value = "租户编码")
    private String code;

    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    private String contactUserName;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String contactPhone;

    /**
     * 企业名称
     */
    @ExcelProperty(value = "企业名称")
    private String companyName;

    /**
     * 统一社会信用代码
     */
    @ExcelProperty(value = "统一社会信用代码")
    private String licenseNumber;

    /**
     * 地址
     */
    @ExcelProperty(value = "地址")
    private String address;

    /**
     * 域名
     */
    @ExcelProperty(value = "域名")
    private String domain;

    /**
     * 企业简介
     */
    @ExcelProperty(value = "企业简介")
    private String intro;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 租户套餐编号
     */
    @ExcelProperty(value = "租户套餐编号")
    private Long packageId;

    /**
     * 过期时间
     */
    @ExcelProperty(value = "过期时间")
    private Date expireTime;

    /**
     * 用户数量（-1不限制）
     */
    @ExcelProperty(value = "用户数量")
    private Long accountCount;

    /**
     * 租户状态（1正常 0停用）
     */
    @ExcelProperty(value = "租户状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=正常,0=停用")
    private String status;


}
