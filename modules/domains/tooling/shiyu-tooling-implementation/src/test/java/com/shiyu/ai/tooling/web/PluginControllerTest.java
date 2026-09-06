package com.shiyu.ai.tooling.web;

import com.shiyu.ai.plugin.market.PluginMarketEntry;
import com.shiyu.ai.plugin.market.PluginMarketService;
import com.shiyu.ai.plugin.registry.PluginRegistry;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.vo.PluginInfoVO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PluginControllerTest {
    private final PluginRegistry registry = mock(PluginRegistry.class);
    private final PluginMarketService market = mock(PluginMarketService.class);
    private final PluginController controller = new PluginController(registry, market);

    @Test
    void listsAndDelegatesLifecycleOperations() {
        PluginDescriptor descriptor = new PluginDescriptor("demo", "Demo", "1.0", "test", "author", "entry", java.util.Map.of());
        when(registry.listPlugins()).thenReturn(List.of(descriptor));
        List<PluginInfoVO> result = controller.listPlugins().getData();
        assertEquals("demo", result.get(0).getId());
        assertEquals("Demo", result.get(0).getName());
        assertEquals("1.0", result.get(0).getVersion());
        assertEquals("INSTALLED", result.get(0).getState());

        assertTrue(controller.startPlugin("demo").isSuccess());
        assertTrue(controller.stopPlugin("demo").isSuccess());
        assertTrue(controller.uninstallPlugin("demo").isSuccess());
        assertTrue(controller.rescan().isSuccess());
        verify(registry).start("demo");
        verify(registry).stop("demo");
        verify(registry).uninstall("demo");
        verify(registry).scanAndLoadPlugins();
    }

    @Test
    void lifecycleFailuresAreReturnedAsStableFailuresAndMarketDelegates() {
        doThrow(new IllegalStateException("bad start")).when(registry).start("bad");
        doThrow(new IllegalStateException("bad stop")).when(registry).stop("bad");
        doThrow(new IllegalStateException("bad uninstall")).when(registry).uninstall("bad");
        doThrow(new IllegalStateException("bad scan")).when(registry).scanAndLoadPlugins();
        assertFalse(controller.startPlugin("bad").isSuccess());
        assertFalse(controller.stopPlugin("bad").isSuccess());
        assertFalse(controller.uninstallPlugin("bad").isSuccess());
        assertFalse(controller.rescan().isSuccess());

        PluginMarketEntry entry = new PluginMarketEntry("demo", "1", "local", "manifest", null, null,
                List.of(), Instant.now(), true);
        when(market.list()).thenReturn(List.of(entry));
        when(market.publish(any(), eq(true))).thenReturn(entry);
        assertEquals(List.of(entry), controller.market().getData());
        assertEquals(entry, controller.publish(entry, true).getData());
        assertTrue(controller.disable("demo").isSuccess());
        verify(market).disable("demo");
    }
}
