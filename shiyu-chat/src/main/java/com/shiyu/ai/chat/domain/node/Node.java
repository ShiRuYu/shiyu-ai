package com.shiyu.ai.chat.domain.node;

import lombok.Data;

@Data
public abstract class Node {
    private String id;
    private String name;
    private String type;
    private String content;
}
