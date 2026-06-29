package com.shiyu.ai.bootstrap.config;

import com.shiyu.ai.aiagent.repository.IntentDefRepository;
import com.shiyu.ai.aiagent.bo.IntentDefBO;
import com.shiyu.ai.aiagent.node.intent.IntentDefinitionFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 搴旂敤鍚姩鏃朵粠 DB 鍔犺浇鎰忓浘瀹氫箟鍒?IntentDefinitionFactory
 */
@Slf4j
@Component
public class IntentDefApplicationRunner implements ApplicationRunner {

    @Resource
    private IntentDefRepository intentDefRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<IntentDefBO> boList = intentDefRepository.selectByAgentId("default");
            if (boList == null || boList.isEmpty()) {
                log.warn("DB 涓湭鎵惧埌鎰忓浘瀹氫箟鏁版嵁锛坅gentId=default锛夛紝浣跨敤绌鸿〃");
                // 鍗充娇娌℃湁鏁版嵁涔熻璋冪敤 reloadFromDb 浠ユ竻绌?static 鍧楀彲鑳介仐鐣欑殑鏁版嵁
                IntentDefinitionFactory.reloadFromDb(List.of());
            } else {
                IntentDefinitionFactory.reloadFromDb(boList);
                log.info("浠?DB 鍔犺浇浜?{} 鏉℃剰鍥惧畾涔夊埌 IntentDefinitionFactory", boList.size());
            }
        } catch (Exception e) {
            log.error("浠?DB 鍔犺浇鎰忓浘瀹氫箟澶辫触锛屽皢浣跨敤绌鸿〃", e);
            IntentDefinitionFactory.reloadFromDb(List.of());
        }
    }
}
