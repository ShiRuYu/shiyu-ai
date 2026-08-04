package com.shiyu.ai.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.shiyu.ai.model.ChatType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String platform;

    private String model;

    private String prompt;

    private ChatType chatType;
}
