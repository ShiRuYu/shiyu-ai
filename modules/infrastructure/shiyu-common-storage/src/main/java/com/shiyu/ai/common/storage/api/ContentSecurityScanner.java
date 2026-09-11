package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

/** Validation SPI for rejecting unsafe file names, types and content before storage. */
public interface ContentSecurityScanner {

    /** Validates an upload and throws when its content violates storage policy. */
    void validate(String fileName, String contentType, byte[] content);
}
