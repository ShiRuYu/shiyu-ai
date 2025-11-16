package com.shiyu.ai.chat.service;

import com.shiyu.ai.chat.domain.node.Intent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentServiceImpl implements IntentService {
    @Override
    public List<Intent> list(String category) {
        return List.of();
    }

    @Override
    public Intent detect(String text, List<Intent> intents) {
        return new Intent();
    }
}
