package com.shiyu.ai.auth.request;
import lombok.Data;
@Data
public class DictRequest {
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String cssClass;
    private String listClass;
    private String isDefault;
    private String remark;
    private Integer status;
}
