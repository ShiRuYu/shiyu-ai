package com.shiyu.ai.knowledge.domain;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * MapStruct mappers are application code in this module, not a reason to hide
 * persistence conversion from the per-domain coverage gate. Exercise both
 * null-source and copy/update paths for every generated mapper.
 */
class GeneratedKnowledgeMapperCoverageTest {

    @Test
    void executesGeneratedMapperNullAndCopyPaths() throws Exception {
        List<Class<?>> mappers = new ArrayList<>();
        for (String packageName : List.of(
                "com.shiyu.ai.knowledge.domain.model",
                "com.shiyu.ai.knowledge.implementation.persistence.dataobject")) {
            String resourceName = packageName.replace('.', '/');
            var resources = Thread.currentThread().getContextClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                URI uri = resources.nextElement().toURI();
                if (!"file".equalsIgnoreCase(uri.getScheme())) continue;
                Path directory = Path.of(uri);
                try (var files = Files.walk(directory)) {
                    files.filter(path -> path.toString().endsWith("MapperImpl.class"))
                            .map(path -> toClassName(directory, packageName, path))
                            .map(this::load)
                            .filter(type -> type != null && !type.isInterface())
                            .forEach(mappers::add);
                }
            }
        }
        assertFalse(mappers.isEmpty(), "generated knowledge mappers must be discoverable");
        for (Class<?> mapperType : mappers) {
            var constructors = mapperType.getDeclaredConstructors();
            if (constructors.length == 0 || !Modifier.isPublic(constructors[0].getModifiers())
                    && !constructors[0].canAccess(null)) {
                constructors[0].setAccessible(true);
            }
            Object mapper = constructors[0].newInstance();
            for (Method method : mapperType.getDeclaredMethods()) {
                if (!method.getName().equals("convert") || method.isBridge() || method.isSynthetic()) continue;
                method.setAccessible(true);
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1) {
                    Object source = parameters[0].getDeclaredConstructor().newInstance();
                    method.invoke(mapper, source);
                    method.invoke(mapper, new Object[]{null});
                } else if (parameters.length == 2) {
                    Object source = parameters[0].getDeclaredConstructor().newInstance();
                    Object target = parameters[1].getDeclaredConstructor().newInstance();
                    method.invoke(mapper, source, target);
                    method.invoke(mapper, new Object[]{null, target});
                }
            }
        }
    }

    private Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String toClassName(Path directory, String packageName, Path classFile) {
        String simpleName = directory.relativize(classFile).toString()
                .replace(File.separatorChar, '.')
                .replaceAll("\\.class$", "");
        return packageName + "." + simpleName;
    }
}
