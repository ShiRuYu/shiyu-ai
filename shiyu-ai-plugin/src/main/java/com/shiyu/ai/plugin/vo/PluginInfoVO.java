package com.shiyu.ai.plugin.vo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class PluginInfoVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String version;
    private String description;
    private String state;
    private String loadedAt;
}
