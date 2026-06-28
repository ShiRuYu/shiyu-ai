package com.shiyu.ai.bootstrap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 搴旂敤鍚姩鐩戝惉鍣?
 * 鍦ㄥ簲鐢ㄥ惎鍔ㄥ畬鎴愬悗鎵撳嵃鐩稿叧璁块棶鍦板潃
 */
@Slf4j
@Component
public class ApplicationStartupListener implements CommandLineRunner {

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("搴旂敤鍚姩鎴愬姛锛?);
        log.info("Knife4j 鏂囨。鍦板潃: http://localhost:{}/doc.html", serverPort);
        log.info("Swagger UI 鍦板潃: http://localhost:{}/swagger-ui/index.html", serverPort);
        log.info("H2 鎺у埗鍙板湴鍧€: http://localhost:{}/h2", serverPort);
        log.info("========================================");
    }
}
