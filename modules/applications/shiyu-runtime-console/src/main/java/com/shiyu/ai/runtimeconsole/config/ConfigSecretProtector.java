package com.shiyu.ai.runtimeconsole.config;

/** 在启动密钥写入配置快照前负责保护其机密性。 */
public interface ConfigSecretProtector {

    /** 保护待持久化的明文密钥。 */
    String protect(String plaintext);

    /** 解保护配置快照中的密文并恢复明文密钥。 */
    String unprotect(String ciphertext);
}
