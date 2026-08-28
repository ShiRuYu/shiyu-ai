package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/** Strongly typed identifier for an authenticated user. */
public record UserId(long value) implements Serializable {

    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
