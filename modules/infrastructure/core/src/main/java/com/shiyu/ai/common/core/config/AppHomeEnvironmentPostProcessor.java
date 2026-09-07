package com.shiyu.ai.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动探测应用根目录（app.home），优先级：app.home 系统属性 > APP_HOME 环境变量 > 项目根目录探测 > user.dir
 * <p>
 * 项目根目录探测逻辑：从 user.dir 开始向上遍历父目录，
 * 找到第一个包含 pom.xml 的目录，即为项目根目录。
 * 这解决了 IDEA 中 user.dir 指向 compile-server 缓存目录导致的路径错误问题。
 * <p>
 * 所有模块统一通过 {@code ${app.home}} 引用该路径。
 * </p>
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class AppHomeEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "appHomeProperties";
    private static final String APP_HOME_KEY = "app.home";
    private static final String APP_HOME_PROPERTY = "app.home";
    private static final String APP_HOME_ENV = "APP_HOME";
    private static final String USER_DIR_KEY = "user.dir";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 0. The bootstrap lock sets this before Spring starts; preserve it so
        // smoke tests and operators can isolate the embedded data directory.
        String appHomeProperty = System.getProperty(APP_HOME_PROPERTY);
        if (appHomeProperty != null && !appHomeProperty.isBlank()) {
            log.info("app.home 系统属性: {}", appHomeProperty);
            setAppHome(environment, appHomeProperty);
            return;
        }

        // 1. 优先使用环境变量 APP_HOME
        String appHomeEnv = environment.getProperty(APP_HOME_ENV);
        if (appHomeEnv != null && !appHomeEnv.isBlank()) {
            log.info("APP_HOME 环境变量: {}", appHomeEnv);
            setAppHome(environment, appHomeEnv);
            return;
        }

        // 2. 从 user.dir 向上找 pom.xml（项目根目录标记）
        String userDir = environment.getProperty(USER_DIR_KEY);
        if (userDir == null || userDir.isBlank()) {
            log.warn("user.dir 为空，使用当前目录: .");
            setAppHome(environment, ".");
            return;
        }

        Path projectRoot = findProjectRoot(Paths.get(userDir));
        if (projectRoot != null) {
            log.info("自动探测到项目根目录: {} (从 {} 向上查找)", projectRoot, userDir);
            setAppHome(environment, projectRoot.toString());
            return;
        }

        // 3. 回退到 user.dir
        log.warn("未找到 pom.xml 标记，回退到 user.dir: {}", userDir);
        setAppHome(environment, userDir);
    }

    /**
     * 从给定路径向上遍历父目录，找到项目根目录。
     * 项目根目录的判断标准：pom.xml 中包含 {@code <packaging>pom</packaging>}（即 parent pom）。
     * 这可以避免错误地将子模块目录识别为项目根目录。
     */
    private Path findProjectRoot(Path start) {
        Path current = start.toAbsolutePath().normalize();
        int maxDepth = 10;
        Path root = null;
        for (int i = 0; i < maxDepth && current != null; i++) {
            Path pom = current.resolve("pom.xml");
            if (Files.exists(pom)) {
                try {
                    String content = Files.readString(pom);
                    if (content.contains("<packaging>pom</packaging>")) {
                        return current; // 找到了 root pom，直接返回
                    }
                } catch (Exception e) {
                    // ignore
                }
                root = current; // 记录最后一个包含 pom.xml 的目录
            }
            current = current.getParent();
        }
        // 如果没找到 root pom，回退到最上层的 pom 目录
        return root;
    }

    private void setAppHome(ConfigurableEnvironment environment, String appHome) {
        Map<String, Object> props = new HashMap<>();
        props.put(APP_HOME_KEY, appHome);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        // Some embedded-storage services intentionally resolve paths without a
        // Spring dependency. Keep their system-property view consistent with
        // the Environment value selected here.
        System.setProperty(APP_HOME_KEY, appHome);
        log.info("app.home = {}", appHome);
    }
}
