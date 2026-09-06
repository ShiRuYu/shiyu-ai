package com.shiyu.ai.model.port;

import java.util.List;

/**
 * Read-only model routing facts exposed to HTTP/application adapters.
 * Provider adapters and their credentials remain private to Model.
 */
public interface ModelRoutingPort {
    List<ModelDescriptor> availableModels();

    String resolvePlatform(String model);

    String defaultPlatform();

    record ModelDescriptor(String id, String platform) {
    }
}
