package com.shiyu.ai.chat.lm;


import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.model.ModelConfig;
import com.shiyu.ai.chat.lm.model.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class ChatEngine implements InitializingBean {

    @Resource
    private Map<String, ModelAdapter> modelAdapterName;

    private final Map<ModelEnum, ModelAdapter> modelAdapterEnum = new EnumMap<>(ModelEnum.class);;

    public String call(String input, ModelEnum modelEnum) {

        ModelRequest request = new ModelRequest(input);
        ModelConfig config = new ModelConfig(modelEnum, null, null, null, null);

        ModelAdapter adapter = modelAdapterEnum.get(modelEnum);
        if (adapter == null) {
            throw new IllegalArgumentException("No ModelAdapter found for: " + modelEnum);
        }

        ModelResult response = adapter.call(config, request);
        return response.getAnswer();
    }

    /**
     * Spring 会在 Bean 注入完成后自动调用这个方法
     */
    @Override
    public void afterPropertiesSet() {

        for (Map.Entry<String, ModelAdapter> entry : modelAdapterName.entrySet()) {
            String beanName = entry.getKey(); // Bean 名称，例如 "localModelAdapter"
            ModelAdapter adapter = entry.getValue();

            try {
                ModelEnum modelEnum = ModelEnum.fromName(beanName);
                modelAdapterEnum.put(modelEnum, adapter);
            } catch (IllegalArgumentException e) {
                System.err.println("No ModelEnum found for bean: " + beanName);
            }
        }
    }
}

