package com.shiyu.ai.model.implementation.infrastructure.chat.cancellation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

/** 验证流式 HTTP 的错误传播、响应体释放和取消作用域的生命周期。 */
class CancellableHttpLifecycleTest {
    private final HttpClient synchronous = mock(HttpClient.class);
    private final java.net.http.HttpClient transport = mock(java.net.http.HttpClient.class);
    private final CancellableJdkHttpClient client = new CancellableJdkHttpClient(synchronous, transport, Duration.ofSeconds(2));

    @Test
    void parsesSuccessfulEventsAndClosesBodyBeforeUnregisteringCancellation() {
        var body = new TrackedBody("data: hello\n\n");
        var listener = new RecordingListener();
        var cancellation = new ProviderRequestCancellation();
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body)));
        inScope(cancellation, () -> client.execute(request(), listener));
        assertEquals(List.of("hello"), listener.events);
        assertEquals(1, listener.opens);
        assertEquals(1, listener.closes);
        assertTrue(body.closed);
        assertTrue(listener.errors.isEmpty());
        int closes = body.closeCount;
        cancellation.cancel();
        assertEquals(closes, body.closeCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {199, 400, 503})
    void failedHttpStatusReportsProviderBodyAndClosesTheResponse(int status) {
        var body = new TrackedBody("provider unavailable");
        var listener = new RecordingListener();
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(status, body)));
        client.execute(request(), listener);
        assertEquals(1, listener.errors.size());
        assertInstanceOf(HttpException.class, listener.errors.getFirst());
        assertTrue(listener.errors.getFirst().getMessage().contains("provider unavailable"));
        assertTrue(body.closed);
        assertEquals(0, listener.opens);
        assertEquals(0, listener.closes);
    }

    @Test
    void failedTransportUnwrapsCompletionErrorAndDoesNotOpenAnEventStream() {
        var failure = new IOException("connection closed");
        for (Throwable error : List.of(failure, new CompletionException(failure))) {
            var listener = new RecordingListener();
            when(transport.<InputStream>sendAsync(any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(error));
            client.execute(request(), listener);
            assertEquals(List.of(failure), listener.errors);
            assertEquals(0, listener.opens);
            assertEquals(0, listener.closes);
        }
    }

    @Test
    void parserFailureIsReportedOnceAndBodyIsReleased() {
        var listener = new RecordingListener();
        var body = new TrackedBody("invalid event");
        var failure = new IllegalArgumentException("malformed stream");
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body)));
        client.execute(request(), (stream, callbacks) -> { throw failure; }, listener);
        assertEquals(List.of(failure), listener.errors);
        assertEquals(1, listener.opens);
        assertEquals(0, listener.closes);
        assertTrue(body.closed);
    }

    @Test
    void cancellationWhileWaitingCancelsTransportWithoutReportingAnError() {
        var listener = new RecordingListener();
        var cancellation = new ProviderRequestCancellation();
        CompletableFuture<HttpResponse<InputStream>> pending = new CompletableFuture<>();
        when(transport.<InputStream>sendAsync(any(), any())).thenReturn(pending);
        inScope(cancellation, () -> client.execute(request(), listener));
        cancellation.cancel();
        cancellation.cancel();
        assertTrue(pending.isCancelled());
        assertTrue(listener.errors.isEmpty());
        assertEquals(0, listener.opens);
        assertEquals(0, listener.closes);
    }

    @Test
    void lateResponseAfterCancellationIsClosedWithoutCallbacks() {
        var listener = new RecordingListener();
        var cancellation = new ProviderRequestCancellation();
        CompletableFuture<HttpResponse<InputStream>> pending = new CompletableFuture<>() {
            @Override
            public boolean cancel(boolean interrupt) { return false; }
        };
        when(transport.<InputStream>sendAsync(any(), any())).thenReturn(pending);
        inScope(cancellation, () -> client.execute(request(), listener));
        cancellation.cancel();
        var body = new TrackedBody("data: late\n\n");
        pending.complete(response(200, body));
        assertTrue(body.closed);
        assertTrue(listener.events.isEmpty());
        assertTrue(listener.errors.isEmpty());
        assertEquals(0, listener.opens);
        assertEquals(0, listener.closes);
    }

    @Test
    void cancellationDuringParsingSuppressesTheParserErrorAndCloseNotification() {
        var listener = new RecordingListener();
        var cancellation = new ProviderRequestCancellation();
        var body = new TrackedBody("data: ignored\n\n");
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body)));
        ServerSentEventParser parser = (stream, callbacks) -> {
            cancellation.cancel();
            throw new IllegalStateException("read interrupted");
        };
        inScope(cancellation, () -> client.execute(request(), parser, listener));
        assertTrue(body.closed);
        assertTrue(listener.errors.isEmpty());
        assertEquals(1, listener.opens);
        assertEquals(0, listener.closes);
    }

    @Test
    void cancellationAtTheEndOfParsingDoesNotReportSuccessfulCompletion() {
        var listener = new RecordingListener();
        var cancellation = new ProviderRequestCancellation();
        var body = new TrackedBody("data: last\n\n");
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body)));
        inScope(cancellation, () -> client.execute(request(), (stream, callbacks) -> cancellation.cancel(), listener));
        assertTrue(body.closed);
        assertTrue(listener.errors.isEmpty());
        assertEquals(1, listener.opens);
        assertEquals(0, listener.closes);
    }

    @Test
    void alreadyCancelledScopeCancelsTheNewTransportFuture() {
        var cancellation = new ProviderRequestCancellation();
        cancellation.cancel();
        var listener = new RecordingListener();
        CompletableFuture<HttpResponse<InputStream>> pending = new CompletableFuture<>();
        when(transport.<InputStream>sendAsync(any(), any())).thenReturn(pending);
        inScope(cancellation, () -> client.execute(request(), listener));
        assertTrue(pending.isCancelled());
        assertTrue(listener.errors.isEmpty());
    }

    @Test
    void cancellationBetweenResponseArrivalAndBodyRegistrationClosesTheBody() {
        var cancellation = new ProviderRequestCancellation();
        var listener = new RecordingListener();
        var body = new TrackedBody("data: late\n\n");
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body, cancellation::cancel)));
        inScope(cancellation, () -> client.execute(request(), listener));
        assertTrue(body.closed);
        assertEquals(0, listener.opens);
        assertTrue(listener.errors.isEmpty());
    }

    @Test
    void unreadableErrorBodyReportsReadFailureEvenIfClosingItAlsoFails() {
        var listener = new RecordingListener();
        var readFailure = new IOException("body unavailable");
        var closes = new AtomicInteger();
        InputStream body = new InputStream() {
            @Override public int read() throws IOException { throw readFailure; }
            @Override public void close() throws IOException {
                closes.incrementAndGet();
                throw new IOException("close unavailable");
            }
        };
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(503, body)));
        client.execute(request(), listener);
        assertEquals(List.of(readFailure), listener.errors);
        assertTrue(closes.get() > 0);
        assertEquals(0, listener.opens);
    }

    @Test
    void getRequestCanHaveNoBodyOrTimeoutAndPreservesHeaders() {
        var listener = new RecordingListener();
        var body = new TrackedBody("data: ok\n\n");
        when(transport.<InputStream>sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response(200, body)));
        Map<String, List<String>> headers = new java.util.LinkedHashMap<>();
        headers.put("X-Request", List.of("first", "second"));
        headers.put("X-Optional", null);
        var request = HttpRequest.builder().method(HttpMethod.GET).url("http://localhost/stream")
                .headers(headers).build();
        new CancellableJdkHttpClient(synchronous, transport, null).execute(request, listener);
        var sent = org.mockito.ArgumentCaptor.forClass(java.net.http.HttpRequest.class);
        verify(transport).<InputStream>sendAsync(sent.capture(), any());
        assertEquals("GET", sent.getValue().method());
        assertTrue(sent.getValue().timeout().isEmpty());
        assertEquals(0, sent.getValue().bodyPublisher().orElseThrow().contentLength());
        assertEquals(List.of("first", "second"), sent.getValue().headers().allValues("X-Request"));
        assertTrue(sent.getValue().headers().allValues("X-Optional").isEmpty());
        assertEquals(List.of("ok"), listener.events);
    }

    @Test
    void unsupportedMultipartAndInvalidUrlsReportErrorsBeforeOpeningTransport() {
        var field = HttpRequest.builder().method(HttpMethod.POST).url("http://localhost/stream")
                .addFormDataField("input", "value").build();
        var file = HttpRequest.builder().method(HttpMethod.POST).url("http://localhost/stream")
                .addFormDataFile("input", "file", "text/plain", new byte[] {1}).build();
        var invalid = HttpRequest.builder().method(HttpMethod.GET).url("not a valid URI").build();
        for (var request : List.of(field, file, invalid)) {
            var listener = new RecordingListener();
            client.execute(request, listener);
            assertEquals(1, listener.errors.size());
            assertInstanceOf(IllegalArgumentException.class, listener.errors.getFirst());
            assertEquals(0, listener.opens);
        }
        verifyNoInteractions(transport);
    }

    @Test
    void nestedScopesRestoreOuterRegistrationAndClosedHandlesAreNotCancelled() {
        var outer = new ProviderRequestCancellation();
        var inner = new ProviderRequestCancellation();
        var outerCalls = new AtomicInteger();
        var innerCalls = new AtomicInteger();
        var closedCalls = new AtomicInteger();
        inScope(outer, () -> {
            ProviderRequestCancellation.registerCurrent(outerCalls::incrementAndGet);
            inScope(inner, () -> ProviderRequestCancellation.registerCurrent(innerCalls::incrementAndGet));
            ProviderRequestCancellation.registerCurrent(outerCalls::incrementAndGet);
            ProviderRequestCancellation.registerCurrent(closedCalls::incrementAndGet).close();
        });
        ProviderRequestCancellation.registerCurrent(closedCalls::incrementAndGet).close();
        outer.cancel();
        outer.cancel();
        assertEquals(2, outerCalls.get());
        assertEquals(0, innerCalls.get());
        assertEquals(0, closedCalls.get());
        inner.cancel();
        assertEquals(1, innerCalls.get());
    }

    private HttpRequest request() {
        return HttpRequest.builder().method(HttpMethod.POST).url("http://localhost/stream")
                .addHeader("Authorization", "Bearer test").body("{}").build();
    }

    private void inScope(ProviderRequestCancellation cancellation, Runnable action) {
        var scope = ProviderRequestCancellation.enter(cancellation);
        try {
            action.run();
        } finally {
            scope.close();
        }
    }

    private HttpResponse<InputStream> response(int status, InputStream body) {
        return response(status, body, () -> {});
    }

    private HttpResponse<InputStream> response(int status, InputStream body, Runnable onBodyLookup) {
        return new HttpResponse<>() {
            @Override public int statusCode() { return status; }
            @Override public InputStream body() { onBodyLookup.run(); return body; }
            @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (key, value) -> true); }
            @Override public java.net.http.HttpRequest request() { return null; }
            @Override public java.util.Optional<HttpResponse<InputStream>> previousResponse() { return java.util.Optional.empty(); }
            @Override public java.util.Optional<javax.net.ssl.SSLSession> sslSession() { return java.util.Optional.empty(); }
            @Override public java.net.URI uri() { return java.net.URI.create("http://localhost/stream"); }
            @Override public java.net.http.HttpClient.Version version() { return java.net.http.HttpClient.Version.HTTP_1_1; }
        };
    }

    /** 记录提供方响应流是否被关闭，保留真实字节读取行为。 */
    private static class TrackedBody extends ByteArrayInputStream {
        private boolean closed;
        private int closeCount;
        TrackedBody(String value) { super(value.getBytes(StandardCharsets.UTF_8)); }
        @Override
        public void close() throws IOException {
            closed = true;
            closeCount++;
            super.close();
        }
    }

    /** 收集业务可观察的流式回调，避免只验证模拟调用。 */
    private static class RecordingListener implements ServerSentEventListener {
        private final List<String> events = new ArrayList<>();
        private final List<Throwable> errors = new ArrayList<>();
        private int opens;
        private int closes;
        @Override public void onOpen(SuccessfulHttpResponse response) { opens++; }
        @Override public void onEvent(ServerSentEvent event) { events.add(event.data()); }
        @Override public void onError(Throwable error) { errors.add(error); }
        @Override public void onClose() { closes++; }
    }
}
