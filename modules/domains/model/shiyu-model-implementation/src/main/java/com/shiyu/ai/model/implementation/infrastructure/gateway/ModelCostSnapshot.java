package com.shiyu.ai.model.implementation.infrastructure.gateway;

public record ModelCostSnapshot(
        String provider,
        String model,
        double inputPricePerMillion,
        double outputPricePerMillion,
        String currency) {}
