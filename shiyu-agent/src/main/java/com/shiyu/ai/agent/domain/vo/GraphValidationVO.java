package com.shiyu.ai.agent.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphValidationVO {

    private boolean valid;

    private List<String> errors;

    private List<String> warnings;

    public static GraphValidationVO success() {
        return GraphValidationVO.builder().valid(true).build();
    }

    public static GraphValidationVO fail(List<String> errors, List<String> warnings) {
        return GraphValidationVO.builder().valid(false).errors(errors).warnings(warnings).build();
    }
}
