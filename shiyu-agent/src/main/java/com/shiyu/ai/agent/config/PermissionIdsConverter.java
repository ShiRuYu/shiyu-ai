package com.shiyu.ai.agent.config;


import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public class PermissionIdsConverter {

    public Long[] map(String str) {
        if (str == null || str.isBlank()) {
            return new Long[0];
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toArray(Long[]::new);
    }

    public String map(Long[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return Arrays.stream(array)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
