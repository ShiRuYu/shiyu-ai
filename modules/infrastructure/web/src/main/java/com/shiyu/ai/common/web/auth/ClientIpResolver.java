package com.shiyu.ai.common.web.auth;

/**
 * Resolves the client address for the current HTTP request.
 *
 * <p>The interface keeps request-scoped transport concerns out of domain and
 * application services.  Implementations belong to the Web support layer.</p>
 */
@FunctionalInterface
public interface ClientIpResolver {

    String currentClientIp();
}
