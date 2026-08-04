package com.shiyu.ai.usage.request;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class UsageQueryRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private int days = 7;
    private int weeks = 4;
    private int months = 6;
}
