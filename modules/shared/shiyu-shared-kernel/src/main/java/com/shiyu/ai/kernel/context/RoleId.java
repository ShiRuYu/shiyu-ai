package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/** Strongly typed identifier for the actor's currently selected role. */
public record RoleId(long value) implements Serializable {

    public RoleId {
        if (value <= 0) {
            throw new IllegalArgumentException("roleId must be positive");
        }
    }
}
