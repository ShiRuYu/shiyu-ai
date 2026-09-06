package com.shiyu.ai.model.gateway;

public record ModelCostSnapshot(String provider, String model, double inputPricePerMillion, double outputPricePerMillion, String currency) { }
