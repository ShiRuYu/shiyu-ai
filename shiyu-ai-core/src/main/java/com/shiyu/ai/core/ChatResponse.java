package com.shiyu.ai.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private boolean success;

    private String content;

    private String platform;

    private String model;

    private String errorMessage;
}
