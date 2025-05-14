package com.AnvilShieldGroup.main_service.filter;

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
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class RequestResponseLoggingFilter implements WebFilter {

    private static final String REQUEST_ID = "requestId";
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

        return chain.filter(exchange.mutate()
                        .request(requestDecorator)
                        .response(responseDecorator)
                        .build())
                .doOnSuccess(aVoid -> logRequestResponse(requestDecorator, responseDecorator, startTime))
                .doOnError(error -> logRequestResponse(requestDecorator, responseDecorator, startTime))
                .doFinally(signalType -> MDC.remove(REQUEST_ID));
    }

    private void logRequestResponse(LoggingRequestDecorator request, LoggingResponseDecorator response, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ID: ").append(MDC.get(REQUEST_ID));
        logBuilder.append(" | Method: ").append(request.getMethod());
        logBuilder.append(" | URI: ").append(request.getURI());

        String requestBody = request.getCachedBody();
        logBuilder.append(" | Req Body: ").append(requestBody != null ? requestBody : "[No Body]");

        logBuilder.append(" | Status: ").append(response.getStatusCode());

        String responseBody = response.getBodyAsString();
        logBuilder.append(" | Res Body: ").append(responseBody != null ? responseBody : "[No Body]");

        logBuilder.append(" | Duration: ").append(duration).append("ms");

        log.info(logBuilder.toString());
    }

    //  Decorator
    private class LoggingRequestDecorator extends ServerHttpRequestDecorator {
        private final AtomicReference<String> cachedBody = new AtomicReference<>();

        public LoggingRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody()
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        cachedBody.set(bodyString);

                        DataBuffer newBuffer = bufferFactory.wrap(bytes);
                        return Mono.just(newBuffer);
                    });
        }

        public String getCachedBody() {
            return cachedBody.get();
        }
    }

    //Decorator
    private class LoggingResponseDecorator extends ServerHttpResponseDecorator {
        private final DataBufferFactory bufferFactory;
        private final AtomicReference<String> bodyAsString = new AtomicReference<>();

        public LoggingResponseDecorator(ServerHttpResponse delegate, DataBufferFactory bufferFactory) {
            super(delegate);
            this.bufferFactory = bufferFactory;
        }

        @Override
        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
            return DataBufferUtils.join(body)
                    .flatMap(buffer -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        DataBufferUtils.release(buffer);

                        bodyAsString.set(new String(bytes, StandardCharsets.UTF_8));
                        return super.writeWith(Mono.just(bufferFactory.wrap(bytes)));
                    });
        }

        public String getBodyAsString() {
            return bodyAsString.get();
        }
    }
}
