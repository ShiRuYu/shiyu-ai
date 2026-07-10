package com.shiyu.ai.model.resilience;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoadBalancer 单元测试
 */
@Tag("dev")
class LoadBalancerTest {

    private LoadBalancer loadBalancer;

    @BeforeEach
    void setUp() {
        loadBalancer = new LoadBalancer();
    }

    @Test
    void testRoundRobinSelect() {
        List<String> platforms = Arrays.asList("openai", "deepseek", "ollama");

        assertEquals("openai", loadBalancer.roundRobinSelect(platforms));
        assertEquals("deepseek", loadBalancer.roundRobinSelect(platforms));
        assertEquals("ollama", loadBalancer.roundRobinSelect(platforms));
    }

    @Test
    void testRoundRobinWrapsAround() {
        List<String> platforms = Arrays.asList("A", "B");

        assertEquals("A", loadBalancer.roundRobinSelect(platforms));
        assertEquals("B", loadBalancer.roundRobinSelect(platforms));
        assertEquals("A", loadBalancer.roundRobinSelect(platforms));
        assertEquals("B", loadBalancer.roundRobinSelect(platforms));
    }

    @Test
    void testSinglePlatform() {
        List<String> platforms = List.of("only-one");

        for (int i = 0; i < 10; i++) {
            assertEquals("only-one", loadBalancer.roundRobinSelect(platforms));
        }
    }

    @Test
    void testEmptyPlatforms() {
        assertThrows(IllegalStateException.class, () ->
            loadBalancer.roundRobinSelect(List.of())
        );
    }

    @Test
    void testNullPlatforms() {
        assertThrows(IllegalStateException.class, () ->
            loadBalancer.roundRobinSelect(null)
        );
    }

    @Test
    void testDistributionIsEven() {
        List<String> platforms = Arrays.asList("A", "B", "C");
        int rounds = 300;

        List<String> results = java.util.stream.IntStream.range(0, rounds)
            .mapToObj(i -> loadBalancer.roundRobinSelect(platforms))
            .collect(Collectors.toList());

        Map<String, Long> counts = results.stream()
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Each platform should be selected exactly 100 times
        assertEquals(100, counts.get("A"));
        assertEquals(100, counts.get("B"));
        assertEquals(100, counts.get("C"));
    }

    @Test
    void testMultipleLoadBalancersIndependence() {
        LoadBalancer lb1 = new LoadBalancer();
        LoadBalancer lb2 = new LoadBalancer();

        List<String> platforms = Arrays.asList("X", "Y");

        assertEquals("X", lb1.roundRobinSelect(platforms));
        assertEquals("X", lb2.roundRobinSelect(platforms)); // Each has own counter
        assertEquals("Y", lb1.roundRobinSelect(platforms));
        assertEquals("Y", lb2.roundRobinSelect(platforms));
    }
}
