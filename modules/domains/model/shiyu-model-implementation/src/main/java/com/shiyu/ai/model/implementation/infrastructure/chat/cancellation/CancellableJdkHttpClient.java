package com.shiyu.ai.model.implementation.infrastructure.chat.cancellation;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.DefaultServerSentEventParser;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Adds request cancellation to LangChain4j's callback based streaming HTTP API. */
final class CancellableJdkHttpClient implements HttpClient {
    private final HttpClient synchronousClient;
    private final java.net.http.HttpClient transport;
    private final Duration readTimeout;

    CancellableJdkHttpClient(
            HttpClient synchronousClient,
            java.net.http.HttpClient transport,
            Duration readTimeout) {
        this.synchronousClient = synchronousClient;
        this.transport = transport;
        this.readTimeout = readTimeout;
    }

    @Override
    public dev.langchain4j.http.client.SuccessfulHttpResponse execute(HttpRequest request) {
        return synchronousClient.execute(request);
    }

    @Override
    public void execute(HttpRequest request, ServerSentEventListener listener) {
        execute(request, new DefaultServerSentEventParser(), listener);
    }

    @Override
    public void execute(
            HttpRequest request,
            ServerSentEventParser parser,
            ServerSentEventListener listener) {
        StreamingCall call = new StreamingCall(parser, listener);
        call.registration = ProviderRequestCancellation.registerCurrent(call::cancel);
        try {
            call.start(request);
        } catch (RuntimeException failure) {
            call.finishWithError(failure);
        }
    }

    private java.net.http.HttpRequest toJdkRequest(HttpRequest request) {
        if (!request.formDataFields().isEmpty() || !request.formDataFiles().isEmpty())
            throw new IllegalArgumentException("streaming multipart requests are not supported");

        java.net.http.HttpRequest.Builder builder =
                java.net.http.HttpRequest.newBuilder(URI.create(request.url()));
        request.headers()
                .forEach(
                        (name, values) -> {
                            if (values != null) values.forEach(value -> builder.header(name, value));
                        });
        if (readTimeout != null) builder.timeout(readTimeout);
        builder.method(
                request.method().name(),
                request.body() == null
                        ? BodyPublishers.noBody()
                        : BodyPublishers.ofString(request.body()));
        return builder.build();
    }

    private final class StreamingCall {
        private final ServerSentEventParser parser;
        private final ServerSentEventListener listener;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean finished = new AtomicBoolean();
        private final AtomicReference<CompletableFuture<HttpResponse<InputStream>>> responseFuture =
                new AtomicReference<>();
        private final AtomicReference<InputStream> responseBody = new AtomicReference<>();
        private ProviderRequestCancellation.Registration registration = () -> {};

        private StreamingCall(ServerSentEventParser parser, ServerSentEventListener listener) {
            this.parser = parser;
            this.listener = listener;
        }

        private void start(HttpRequest request) {
            CompletableFuture<HttpResponse<InputStream>> future =
                    transport.sendAsync(
                            toJdkRequest(request), HttpResponse.BodyHandlers.ofInputStream());
            responseFuture.set(future);
            if (cancelled.get()) future.cancel(true);
            future.whenComplete(this::handleResponse);
        }

        private void handleResponse(HttpResponse<InputStream> response, Throwable failure) {
            if (cancelled.get()) {
                closeBody(response == null ? null : response.body());
                finish();
                return;
            }
            if (failure != null) {
                finishWithError(unwrap(failure));
                return;
            }

            InputStream body = response.body();
            responseBody.set(body);
            if (cancelled.get()) {
                closeBody(responseBody.getAndSet(null));
                finish();
                return;
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                try (body) {
                    finishWithError(
                            new HttpException(
                                    response.statusCode(),
                                    new String(body.readAllBytes(), StandardCharsets.UTF_8)));
                } catch (IOException readFailure) {
                    finishWithError(readFailure);
                }
                return;
            }

            try {
                listener.onOpen(
                        SuccessfulHttpResponse.builder()
                                .statusCode(response.statusCode())
                                .headers(response.headers().map())
                                .body(null)
                                .build());
                parser.parse(body, listener);
                if (!cancelled.get()) listener.onClose();
            } catch (RuntimeException parseFailure) {
                if (!cancelled.get()) finishWithError(parseFailure);
            } finally {
                closeBody(body);
                finish();
            }
        }

        private void finishWithError(Throwable failure) {
            if (cancelled.get() || !finished.compareAndSet(false, true)) return;
            try {
                listener.onError(failure);
            } finally {
                cleanup();
            }
        }

        private void cancel() {
            if (!cancelled.compareAndSet(false, true)) return;
            CompletableFuture<?> future = responseFuture.get();
            if (future != null) future.cancel(true);
            closeBody(responseBody.getAndSet(null));
            finish();
        }

        private void finish() {
            if (!finished.compareAndSet(false, true)) return;
            cleanup();
        }

        private void cleanup() {
            registration.close();
            closeBody(responseBody.getAndSet(null));
        }

        private void closeBody(InputStream body) {
            if (body == null) return;
            try {
                body.close();
            } catch (IOException ignored) {
            }
        }

        private Throwable unwrap(Throwable failure) {
            Throwable cause = failure.getCause();
            return cause == null ? failure : cause;
        }
    }
}
