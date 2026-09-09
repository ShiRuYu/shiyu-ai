package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

public record TrendResponse(
        List<String> dates,
        List<Double> values
) {}

