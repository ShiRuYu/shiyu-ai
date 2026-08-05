package com.shiyu.ai.bootstrap.mapping;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.auth.api.response.AuthRoleResponse;
import com.shiyu.ai.auth.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.auth.api.response.AuthTenantResponse;
import com.shiyu.ai.auth.api.response.AuthUserResponse;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.knowledge.api.response.KnowledgeAuditResponse;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.vo.AiModelVO;
import com.shiyu.ai.model.vo.AiPlatformVO;
import io.github.linpeilie.BaseMapper;
import io.github.linpeilie.Converter;
import io.github.linpeilie.ConverterFactory;
import io.github.linpeilie.mapstruct.MapstructAutoConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies the MapStruct Plus registry against every production call that uses
 * {@code MapstructUtils.convert(source, Target.class)}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamicConverterRegistryTest {

    private static final Pattern CONVERT_TARGET = Pattern.compile(
            "MapstructUtils\\.convert\\s*\\((?:(?!;).)*?,\\s*([A-Za-z_$][\\w$]*)\\.class\\s*\\)",
            Pattern.DOTALL);
    private static final Pattern IMPORT = Pattern.compile("(?m)^import\\s+([\\w.*]+);");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^package\\s+([\\w.]+);");

    private AnnotationConfigApplicationContext context;
    private ConverterFactory converterFactory;
    private Converter converter;
    private Set<Class<?>> registeredTargets;

    @BeforeAll
    void setUpConverterRegistry() {
        context = new AnnotationConfigApplicationContext();
        context.register(MapstructAutoConfiguration.class);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context, false);
        scanner.addIncludeFilter(new AssignableTypeFilter(BaseMapper.class));
        scanner.scan("com.shiyu.ai");
        context.refresh();

        converterFactory = context.getBean(ConverterFactory.class);
        converter = context.getBean(Converter.class);
        registeredTargets = context.getBeansOfType(BaseMapper.class).values().stream()
                .map(mapper -> ResolvableType.forClass(mapper.getClass())
                        .as(BaseMapper.class)
                        .getGeneric(1)
                        .resolve())
                .filter(target -> target != null)
                .collect(java.util.stream.Collectors.toSet());
    }

    @AfterAll
    void closeConverterRegistry() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void registersTargetsForEveryProductionDynamicConversion() throws IOException {
        Set<Class<?>> dynamicTargets = findDynamicConversionTargets();
        Set<String> missingTargets = dynamicTargets.stream()
                .filter(target -> !registeredTargets.contains(target))
                .map(Class::getName)
                .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));

        assertFalse(dynamicTargets.isEmpty(), "No MapstructUtils conversion targets were discovered");
        assertEquals(Set.of(), missingTargets,
                () -> "MapstructUtils targets without a registered mapper: " + missingTargets);
    }

    @Test
    void registersEveryPreviouslyMissingAndAuthenticationPair() {
        assertAll(
                () -> assertMapper(AiModelRequest.class, AiModelBO.class),
                () -> assertMapper(AiModelBO.class, AiModelResponse.class),
                () -> assertMapper(AiModelResponse.class, AiModelVO.class),
                () -> assertMapper(AiPlatformRequest.class, AiPlatformBO.class),
                () -> assertMapper(AiPlatformBO.class, AiPlatformResponse.class),
                () -> assertMapper(AiPlatformResponse.class, AiPlatformVO.class),
                () -> assertMapper(IntentDefRequest.class, IntentDefBO.class),
                () -> assertMapper(IntentDefBO.class, IntentDefVO.class),
                () -> assertMapper(KnowledgeAuditLogBO.class, KnowledgeAuditResponse.class),
                () -> assertMapper(UserBO.class, AuthUserResponse.class),
                () -> assertMapper(TenantBO.class, AuthTenantResponse.class),
                () -> assertMapper(RoleBO.class, AuthRoleResponse.class),
                () -> assertMapper(UserScopeRoleBO.class, AuthScopeRoleResponse.class));
    }

    @Test
    void mapsModelPlatformIntentAndAuditFieldsWithoutLosingValues() {
        AiModelRequest modelRequest = new AiModelRequest();
        modelRequest.setPlatformId(9L);
        modelRequest.setModelName("gpt-test");
        modelRequest.setDisplayName("Test model");
        modelRequest.setStatus("1");
        AiModelBO model = converter.convert(modelRequest, AiModelBO.class);

        AiModelResponse modelResponse = converter.convert(model, AiModelResponse.class);
        AiModelVO modelView = converter.convert(modelResponse, AiModelVO.class);
        List<AiModelResponse> modelResponses = converter.convert(List.of(model), AiModelResponse.class);
        AiModelBO nullStatusModel = new AiModelBO();
        AiModelResponse nullStatusModelResponse = converter.convert(nullStatusModel, AiModelResponse.class);

        AiPlatformRequest platformRequest = new AiPlatformRequest();
        platformRequest.setName("OpenAI");
        platformRequest.setCode("openai");
        platformRequest.setStatus("0");
        AiPlatformBO platform = converter.convert(platformRequest, AiPlatformBO.class);
        AiPlatformResponse platformResponse = converter.convert(platform, AiPlatformResponse.class);
        AiPlatformVO platformView = converter.convert(platformResponse, AiPlatformVO.class);

        IntentDefRequest intentRequest = new IntentDefRequest();
        intentRequest.setAgentId("default");
        intentRequest.setCode("greeting");
        intentRequest.setName("Greeting");
        intentRequest.setStatus(1);
        IntentDefBO intent = converter.convert(intentRequest, IntentDefBO.class);
        IntentDefVO intentResponse = converter.convert(intent, IntentDefVO.class);

        KnowledgeAuditLogBO audit = new KnowledgeAuditLogBO();
        audit.setId(10L);
        audit.setTenantId(1L);
        audit.setSpaceId(2L);
        audit.setResourceType("document");
        audit.setResourceId(3L);
        audit.setAction("CREATE");
        audit.setDetailJson("{\"source\":\"test\"}");
        audit.setStatus(1);
        audit.setDelFlag(0);
        audit.setCreateTime(LocalDateTime.of(2026, 8, 4, 0, 0));
        KnowledgeAuditResponse auditResponse = converter.convert(audit, KnowledgeAuditResponse.class);

        assertAll(
                () -> assertEquals(9L, model.getPlatformId()),
                () -> assertEquals(1, model.getStatus()),
                () -> assertEquals("1", modelResponse.getStatus()),
                () -> assertEquals("gpt-test", modelView.getModelName()),
                () -> assertEquals("gpt-test", modelResponse.getModelName()),
                () -> assertEquals(1, modelResponses.size()),
                () -> assertEquals("gpt-test", modelResponses.getFirst().getModelName()),
                () -> assertNull(nullStatusModelResponse.getStatus()),
                () -> assertEquals("OpenAI", platform.getName()),
                () -> assertEquals(0, platform.getStatus()),
                () -> assertEquals("0", platformResponse.getStatus()),
                () -> assertEquals("OpenAI", platformView.getName()),
                () -> assertEquals("greeting", intent.getCode()),
                () -> assertEquals("1", intentResponse.getStatus()),
                () -> assertEquals(1L, auditResponse.getTenantId()),
                () -> assertEquals("document", auditResponse.getResourceType()),
                () -> assertEquals(1, auditResponse.getStatus()),
                () -> assertEquals(0, auditResponse.getDelFlag()));
    }

    private <S, T> void assertMapper(Class<S> sourceType, Class<T> targetType) {
        assertNotNull(converterFactory.getMapper(sourceType, targetType),
                () -> "Missing mapper from " + sourceType.getName() + " to " + targetType.getName());
    }

    private Set<Class<?>> findDynamicConversionTargets() throws IOException {
        Path projectRoot = findProjectRoot();
        List<Path> sourceFiles;
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            sourceFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().replace('\\', '/').contains("/src/main/java/"))
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .toList();
        }

        Set<Class<?>> targets = new HashSet<>();
        for (Path sourceFile : sourceFiles) {
            targets.addAll(readConversionTargets(sourceFile));
        }
        return targets;
    }

    private Set<Class<?>> readConversionTargets(Path sourceFile) throws IOException {
        String source = Files.readString(sourceFile);
        Map<String, String> imports = new HashMap<>();
        List<String> wildcardImports = new ArrayList<>();
        Matcher importMatcher = IMPORT.matcher(source);
        while (importMatcher.find()) {
            String qualifiedName = importMatcher.group(1);
            if (qualifiedName.endsWith(".*")) {
                wildcardImports.add(qualifiedName.substring(0, qualifiedName.length() - 2));
            } else {
                imports.put(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1), qualifiedName);
            }
        }

        Matcher packageMatcher = PACKAGE.matcher(source);
        String packageName = packageMatcher.find() ? packageMatcher.group(1) : "";
        Set<Class<?>> targets = new HashSet<>();
        Matcher convertMatcher = CONVERT_TARGET.matcher(source);
        while (convertMatcher.find()) {
            String simpleName = convertMatcher.group(1);
            targets.add(resolveTargetType(simpleName, imports, wildcardImports, packageName, sourceFile));
        }
        return targets;
    }

    private Class<?> resolveTargetType(String simpleName, Map<String, String> imports,
                                       List<String> wildcardImports, String packageName, Path sourceFile) {
        List<String> candidates = new ArrayList<>();
        if (imports.containsKey(simpleName)) {
            candidates.add(imports.get(simpleName));
        }
        candidates.add(packageName + "." + simpleName);
        wildcardImports.forEach(packagePrefix -> candidates.add(packagePrefix + "." + simpleName));

        for (String candidate : candidates) {
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException ignored) {
                // Continue through explicit, local, and wildcard imports.
            }
        }
        throw new AssertionError("Cannot resolve conversion target " + simpleName + " in " + sourceFile);
    }

    private Path findProjectRoot() {
        for (Path current = Path.of("").toAbsolutePath().normalize(); current != null; current = current.getParent()) {
            if (Files.isDirectory(current.resolve("platform"))
                    && Files.isDirectory(current.resolve("business"))
                    && Files.isDirectory(current.resolve("infrastructure"))) {
                return current;
            }
        }
        throw new IllegalStateException("Unable to locate the project root");
    }
}
