package com.AnvilShieldGroup.rate_service.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class RequestResponseLoggingFilter implements WebFilter {

    private static final String REQUEST_ID = "requestId";
    // Removed MAX_PAYLOAD_LENGTH
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);

        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        LoggingRequestDecorator requestDecorator = new LoggingRequestDecorator(request);
        LoggingResponseDecorator responseDecorator = new LoggingResponseDecorator(response, bufferFactory);

        return chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build()).doOnSuccess(aVoid -> {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(requestDecorator, responseDecorator, duration);
        }).doOnError(throwable -> {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(requestDecorator, responseDecorator, duration);
        }).doFinally(signalType -> {
            MDC.remove(REQUEST_ID);
        });
    }

    private void logRequestResponse(LoggingRequestDecorator request, LoggingResponseDecorator response, long duration) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ID: ").append(MDC.get(REQUEST_ID));
        logBuilder.append(" Method: ").append(request.getMethod());
        logBuilder.append(" URI:").append(request.getURI());

        String requestBody = request.getCachedBody();
        logBuilder.append(" | Req Body: ");
        if (requestBody != null) {
            logBuilder.append(requestBody);
        } else {
            logBuilder.append("[No Body]");
        }
        logBuilder.append(" | Status: ").append(response.getStatusCode());
        logBuilder.append(" | Duration: ").append(duration).append("ms");

        String responseBody = response.getBodyAsString();
        logBuilder.append(" | Res Body: ").append(responseBody); // Simplified response body logging

        log.info(logBuilder.toString());
    }
    private class LoggingRequestDecorator extends ServerHttpRequestDecorator {
        private final AtomicReference<String> cachedBody = new AtomicReference<>();
        public LoggingRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }
        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().buffer().map(dataBuffers -> {
                DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                byte[] bytes = new byte[joinedBuffer.readableByteCount()];
                joinedBuffer.read(bytes);
                DataBufferUtils.release(joinedBuffer);
                return bytes;
            }).singleOrEmpty().map(bytes -> {
                String body = new String(bytes, StandardCharsets.UTF_8);
                cachedBody.set(body);
                return bufferFactory.wrap(bytes);
            }).flux();
        }

        public String getCachedBody() {
            return cachedBody.get();
        }
    }

    private class LoggingResponseDecorator extends ServerHttpResponseDecorator {

        private final DataBufferFactory bufferFactory;
        private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
        private final AtomicReference<String> bodyAsString = new AtomicReference<>();

        public LoggingResponseDecorator(ServerHttpResponse delegate, DataBufferFactory bufferFactory) {
            super(delegate);
            this.bufferFactory = bufferFactory;
        }

        @Override
        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
            return super.writeWith(DataBufferUtils.join(body).doOnNext(buffer -> {
                try {
                    Channels.newChannel(cachedBody).write(buffer.asByteBuffer());
                } catch (IOException e) {
                    log.error("Error caching response body (ID: {})", MDC.get(REQUEST_ID), e);
                } finally {
                    DataBufferUtils.release(buffer);
                }
            }).map(buffer -> bufferFactory.wrap(cachedBody.toByteArray())));
        }

        public String getBodyAsString() {
            // Simplified: Always return the string conversion
            if (bodyAsString.get() == null) {
                bodyAsString.set(new String(cachedBody.toByteArray(), StandardCharsets.UTF_8));
            }
            return bodyAsString.get();
        }
    }
}