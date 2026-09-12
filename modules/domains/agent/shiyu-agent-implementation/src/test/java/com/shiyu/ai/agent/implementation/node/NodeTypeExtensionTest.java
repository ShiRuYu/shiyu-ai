package com.shiyu.ai.agent.implementation.node;

import com.shiyu.ai.agent.contract.node.NodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NodeTypeExtensionTest {

    @Test
    void preservesUnknownTypeCodesForOptionalModules() {
        NodeType type = NodeType.fromCode("EDUCATION_TEACH");

        assertEquals("EDUCATION_TEACH", type.getCode());
        assertNotEquals(NodeType.DEFAULT, type);
    }

    @Test
    void platformBuiltInsDoNotContainEducationTypes() {
        assertFalse(java.util.Arrays.stream(NodeType.values())
                .anyMatch(type -> type.getCode().startsWith("EDUCATION_")
                        || type.getCode().equals("ABILITY_QUERY")
                        || type.getCode().equals("SCORE_ANALYSIS")
                        || type.getCode().equals("REVIEW_SCHEDULE")
                        || type.getCode().equals("PREREQ_CHECK")));
    }
}
