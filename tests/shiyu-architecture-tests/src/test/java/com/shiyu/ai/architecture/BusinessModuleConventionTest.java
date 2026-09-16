package com.shiyu.ai.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 约束新增业务实现沿用自动配置和模块开关协议。 */
class BusinessModuleConventionTest {

    @Test
    void everyBusinessImplementationPublishesAConditionalAutoConfiguration() throws IOException {
        Path root = repositoryRoot();
        try (Stream<Path> businessAreas = Files.list(root.resolve("modules/business"))) {
            List<Path> implementations = businessAreas
                    .filter(Files::isDirectory)
                    .flatMap(area -> {
                        try {
                            return Files.list(area)
                                    .filter(Files::isDirectory)
                                    .filter(path -> path.getFileName().toString().endsWith("-implementation"));
                        } catch (IOException exception) {
                            throw new IllegalStateException("cannot inspect business area: " + area, exception);
                        }
                    })
                    .toList();
            assertFalse(implementations.isEmpty());
            for (Path implementation : implementations) {
                String moduleName = implementation.getFileName().toString()
                        .replace("-implementation", "")
                        .replaceFirst("^shiyu-", "");
                String classSuffix = toTypeName(moduleName) + "ModuleAutoConfiguration.java";
                try (Stream<Path> configs = Files.walk(implementation.resolve("src/main/java"))) {
                    Path config = configs.filter(path -> path.getFileName().toString().equals(classSuffix))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("missing module auto configuration: " + implementation));
                    String source = Files.readString(config);
                    assertTrue(source.contains("@ConditionalOnBusinessModule"), config.toString());
                    assertTrue(source.contains("BusinessModuleDescriptor"), config.toString());
                }
                Path imports = implementation.resolve(
                        "src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
                assertTrue(Files.isRegularFile(imports), imports.toString());
                assertTrue(Files.readString(imports).contains(classSuffix.replace(".java", "")), imports.toString());
                try (Stream<Path> tests = Files.walk(implementation.resolve("src/test/java"))) {
                    assertTrue(tests.anyMatch(path -> path.getFileName().toString().equals(classSuffix.replace(".java", "Test.java"))),
                            "missing module convention test: " + implementation);
                }
            }
        }
    }

    @Test
    void compositionRootAndPlatformConfigurationDoNotNameBusinessImplementations() throws IOException {
        Path root = repositoryRoot();
        String bootstrap = Files.readString(root.resolve(
                "modules/applications/shiyu-ai-bootstrap/src/main/java/com/shiyu/ai/bootstrap/ShiyuBootstrapApplication.java"));
        String platform = Files.readString(root.resolve(
                "modules/applications/shiyu-platform-composition/src/main/java/com/shiyu/ai/composition/config/PlatformCompositionAutoConfiguration.java"));
        assertFalse(bootstrap.contains("com.shiyu.ai.education.implementation"));
        assertFalse(platform.contains("com.shiyu.ai.education.implementation"));
    }

    private Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("modules"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new AssertionError("repository root not found");
    }

    private String toTypeName(String moduleName) {
        return Stream.of(moduleName.split("-"))
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .reduce("", String::concat);
    }
}
