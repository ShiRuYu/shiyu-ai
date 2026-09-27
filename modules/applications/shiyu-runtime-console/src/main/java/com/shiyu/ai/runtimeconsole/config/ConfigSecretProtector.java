package com.shiyu.ai.runtimeconsole.config;

/** Protects startup secrets before they are persisted in a configuration snapshot. */
public interface ConfigSecretProtector {

    String protect(String plaintext);

    String unprotect(String ciphertext);
}
