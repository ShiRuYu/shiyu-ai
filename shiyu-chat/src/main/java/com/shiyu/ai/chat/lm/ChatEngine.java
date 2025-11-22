package com.shiyu.ai.chat.lm;


import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ChatEngine {

    @Resource
    private Map<String, ModelAdapter> modelAdapterMap;

    public String call(String input, ModelEnum modelEnum) {

        ModelRequest request = new ModelRequest(input);

        ModelAdapter adapter = modelAdapterMap.get(modelEnum.getAdapterName());
        if (adapter == null) {
            throw new IllegalArgumentException("No ModelAdapter found for: " + modelEnum);
        }

        ModelResult response = adapter.call(request);
        return response.getAnswer();
    }

    public Flux<String> stream(String input, ModelEnum modelEnum) {

        ModelRequest request = new ModelRequest(input);

        ModelAdapter adapter = modelAdapterMap.get(modelEnum.getAdapterName());
        if (adapter == null) {
            throw new IllegalArgumentException("No ModelAdapter found for: " + modelEnum);
        }

        ModelResult response = adapter.stream(request);
        return response.getAnswerStream();
    }
}

