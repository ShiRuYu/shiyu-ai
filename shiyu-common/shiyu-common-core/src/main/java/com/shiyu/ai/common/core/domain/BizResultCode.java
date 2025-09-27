package com.shiyu.ai.common.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务响应码
 *
 */
@Getter
@RequiredArgsConstructor
public enum BizResultCode {

    /**
     * 正常响应码
     */
    SUC(200, "成功"),
    ERR(400, "请求失败"),

    ERR_10001(10001, "用户已存在"),
    ERR_10002(10002, "用户名或密码错误"),
    ERR_10003(10003, "验证码错误"),
    ERR_10004(10004, "密码验证失败"),
    ERR_10005(10005, "角色不存在"),

    ERR_10006(10006, "参数绑定失败"),
    ERR_10007(10007, "参数校验失败"),
    ERR_10008(10008, "参数执行异常"),

    ERR_10009(10009, "业务异常"),


    ERR_11003(11003, "无权限，请联系管理员申请权限"),
    ERR_11004(11004, "越权操作"),
    ERR_11005(11005, "您目前暂无此角色或已被禁用，请联系管理员"),
    ERR_11006(11006, "非法操作"),


    ERR_30001(30001, "预览环境不支持此操作");

    private final int code;

    private final String msg;

}
