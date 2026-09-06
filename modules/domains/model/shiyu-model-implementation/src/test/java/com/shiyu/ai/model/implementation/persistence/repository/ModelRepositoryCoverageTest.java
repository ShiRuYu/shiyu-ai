package com.shiyu.ai.model.implementation.persistence.repository;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiModelDO;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiPlatformDO;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class ModelRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(7L);

    @Test
    void coversTenantScopedModelAndPlatformRepositoryQueriesAndMutations() throws Exception {
        AiModelRepositoryImpl models = new AiModelRepositoryImpl();
        com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper modelMapper =
                mock(com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper.class, mapperAnswer(false));
        inject(models, "aiModelMapper", modelMapper);
        AiPlatformRepositoryImpl platforms = new AiPlatformRepositoryImpl();
        com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper platformMapper =
                mock(com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper.class, mapperAnswer(true));
        inject(platforms, "aiPlatformMapper", platformMapper);

        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(inv -> {
                        Class<?> type = inv.getArgument(1);
                        if (inv.getArgument(0) == null) return null;
                        return type.getDeclaredConstructor().newInstance();
                    });
            conversions.when(() -> MapstructUtils.convert(any(List.class), any(Class.class)))
                    .thenAnswer(inv -> {
                        List<?> source = inv.getArgument(0);
                        Class<?> type = inv.getArgument(1);
                        if (source == null) return List.of();
                        java.util.ArrayList<Object> result = new java.util.ArrayList<>();
                        for (Object ignored : source) result.add(type.getDeclaredConstructor().newInstance());
                        return result;
                    });

            assertEquals(1L, models.selectPage(TENANT, null, null, null).getLeft());
            assertEquals(1L, models.selectPage(TENANT, 2L, 1, 10).getLeft());
            assertNotNull(models.selectById(TENANT, 1L));
            assertNotNull(models.selectDefaultByPlatformId(TENANT, 2L));
            AiModelBO model = new AiModelBO();
            assertSame(model, models.create(TENANT, model));
            assertSame(model, models.update(TENANT, model));
            models.deleteById(TENANT, 1L); models.deleteByIds(TENANT, List.of(1L, 2L));
            assertEquals(1, models.selectOptions(TENANT, null).size());
            models.clearDefaultExcept(TENANT, 2L, null); models.clearDefaultExcept(TENANT, 2L, 1L);

            assertEquals(1L, platforms.selectPage(TENANT, null, null, null, null).getLeft());
            assertEquals(1L, platforms.selectPage(TENANT, 1, 10, "name", "code").getLeft());
            assertEquals(1, platforms.selectAllEnabled(TENANT).size());
            assertNotNull(platforms.selectById(TENANT, 1L));
            assertNotNull(platforms.selectByCode(TENANT, "code"));
            assertNotNull(platforms.selectDefault(TENANT));
            AiPlatformBO platform = new AiPlatformBO();
            assertSame(platform, platforms.create(TENANT, platform));
            assertSame(platform, platforms.update(TENANT, platform));
            platforms.deleteById(TENANT, 1L); assertEquals(1, platforms.selectOptions(TENANT).size());
            platforms.clearDefaultExcept(TENANT, null); platforms.clearDefaultExcept(TENANT, 1L);
        }
    }

    private static Answer<Object> mapperAnswer(boolean platformMapper) {
        return invocation -> {
            String name = invocation.getMethod().getName();
            if (name.equals("selectCountByQuery")) return 1L;
            if (name.startsWith("selectListByQuery")) {
                if (platformMapper) {
                    AiPlatformDO value = new AiPlatformDO(); value.setId(1L); value.setName("Platform"); value.setCode("code"); return List.of(value);
                }
                AiModelDO value = new AiModelDO(); value.setId(1L); value.setDisplayName("Model"); value.setModelName("model"); return List.of(value);
            }
            if (name.startsWith("selectOne")) {
                if (platformMapper) {
                    AiPlatformDO value = new AiPlatformDO();
                    value.setId(1L); value.setName("Platform"); value.setCode("code");
                    return value;
                }
                return new AiModelDO();
            }
            if (name.startsWith("insert") || name.startsWith("update") || name.startsWith("delete")) return 1;
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        };
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
