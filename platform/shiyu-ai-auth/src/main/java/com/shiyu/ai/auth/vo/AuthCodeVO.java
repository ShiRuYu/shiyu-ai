package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限码视图对象
 */
@Data
public class AuthCodeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> codes;
}
