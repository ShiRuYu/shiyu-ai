package com.shiyu.ai.model.contract.api;

import java.util.List;

/**
 * Read-only model routing facts exposed to HTTP/application adapters. Provider adapters and their
 * credentials remain private to Model.
 */
public interface ModelRoutingPort {
    /** Returns models currently exposed to API and application adapters. */
    List<ModelDescriptor> availableModels();

    /** Resolves the provider code for a requested model id. */
    String resolvePlatform(String model);

    /** Returns the provider used when a request does not specify one. */
    String defaultPlatform();

    /** Public, credential-free description of a routable model. */
    record ModelDescriptor(String id, String platform) {}
}
