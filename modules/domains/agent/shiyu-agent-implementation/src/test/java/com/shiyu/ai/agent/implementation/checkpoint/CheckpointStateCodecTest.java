package com.shiyu.ai.agent.implementation.checkpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 验证检查点恢复保留标量身份，并拒绝损坏或无法无损往返的图状态。 */
class CheckpointStateCodecTest {
    @Test
    void restoresNestedLongAcrossEscapedJsonPointerPath() {
        Map<String, Object> original = Map.of("a/b~c", List.of(Map.of("studentId", 42L)));
        Map<String, String> hints = CheckpointStateCodec.typeHints(original);

        assertEquals(Map.of("/a~1b~0c/0/studentId", "Long"), hints);
        Map<String, Object> jsonDecoded = Map.of("a/b~c", List.of(Map.of("studentId", 42)));
        Map<String, Object> restored = CheckpointStateCodec.restore(jsonDecoded, hints);
        assertEquals(42L, ((Map<?, ?>) ((List<?>) restored.get("a/b~c")).get(0)).get("studentId"));
    }

    @Test
    void refusesCheckpointWithForgedOrCorruptedScalarTypeHints() {
        Map<String, Object> decoded = Map.of("studentId", 42);
        assertThrows(IllegalStateException.class,
                () -> CheckpointStateCodec.restore(decoded, Map.of("/other", "Long")));
        assertThrows(IllegalStateException.class,
                () -> CheckpointStateCodec.restore(decoded, Map.of("/studentId", "Double")));
        assertThrows(IllegalStateException.class,
                () -> CheckpointStateCodec.restore(Map.of("studentId", "042"),
                        Map.of("/studentId", "Long")));
        assertThrows(IllegalStateException.class,
                () -> CheckpointStateCodec.restore(Map.of("studentId", "not-a-number"),
                        Map.of("/studentId", "Long")));
    }

    @Test
    void refusesAliasedAndNonJsonValuesBeforeAllowingAutomaticResume() {
        List<Object> repeated = new ArrayList<>();
        repeated.add(Map.of("value", 1));
        repeated.add(repeated.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> CheckpointStateCodec.typeHints(Map.of("items", repeated)));
        List<Object> cyclic = new ArrayList<>();
        cyclic.add(cyclic);
        assertThrows(IllegalArgumentException.class,
                () -> CheckpointStateCodec.typeHints(Map.of("items", cyclic)));
        assertThrows(IllegalArgumentException.class,
                () -> CheckpointStateCodec.typeHints(Map.of("object", new Object())));
        Map<Object, Object> nonStringKey = new HashMap<>();
        nonStringKey.put(7, "value");
        assertThrows(IllegalArgumentException.class,
                () -> CheckpointStateCodec.typeHints(Map.of("nested", nonStringKey)));
    }
}
