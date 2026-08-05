package com.shiyu.ai.knowledge.security;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KnowledgeAccessContextTest {

    @Test
    void remainsSerializableWhenStoredInAgentGraphState() throws Exception {
        KnowledgeAccessContext source = new KnowledgeAccessContext(1L, 2L, 3L, true);
        byte[] bytes;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ObjectOutputStream objectOutput = new ObjectOutputStream(output)) {
            objectOutput.writeObject(source);
            bytes = output.toByteArray();
        }

        try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(source, objectInput.readObject());
        }
    }
}
